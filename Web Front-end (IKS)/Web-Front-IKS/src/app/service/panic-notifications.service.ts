import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, OnDestroy } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { AuthService } from './auth.service';
import { PanicNotificationDTO, PageResponse } from '../models/panic.models';
import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export interface PanicNotificationsQuery {
  page?: number;
  size?: number;
  sort?: string;
  fromDate?: string;
  toDate?: string;
}

@Injectable({ providedIn: 'root' })
export class PanicNotificationsService implements OnDestroy {
  private apiUrl = 'http://localhost:8080/api/panic-notifications';
  private wsUrl = 'http://localhost:8080/ws';
  
  private stompClient: Client | null = null;
  private panicSubscription: StompSubscription | null = null;
  private resolvedSubscription: StompSubscription | null = null;
  
  private panicNotifications$ = new Subject<PanicNotificationDTO>();
  private panicResolvedNotifications$ = new Subject<PanicNotificationDTO>();
  private connectionStatus$ = new Subject<boolean>();

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  getPanicNotifications(query: PanicNotificationsQuery): Observable<PageResponse<PanicNotificationDTO>> {
    const authToken = this.authService.getToken();
    const headers = authToken ? new HttpHeaders({ Authorization: `Bearer ${authToken}` }) : undefined;

    let params = new HttpParams();
    if (query.page !== undefined) params = params.set('page', query.page.toString());
    if (query.size !== undefined) params = params.set('size', query.size.toString());
    if (query.sort) params = params.set('sort', query.sort);
    if (query.fromDate) params = params.set('fromDate', query.fromDate);
    if (query.toDate) params = params.set('toDate', query.toDate);

    return this.http.get<PageResponse<PanicNotificationDTO>>(
      `${this.apiUrl}/retrieve-all`,
      { headers, params }
    );
  }

  resolvePanic(id: number): Observable<PanicNotificationDTO> {
    const authToken = this.authService.getToken();
    const headers = authToken ? new HttpHeaders({ Authorization: `Bearer ${authToken}` }) : undefined;

    return this.http.post<PanicNotificationDTO>(
      `${this.apiUrl}/resolve/${id}`,
      {},
      { headers }
    );
  }

  // WebSocket methods
  connectToWebSocket(): void {
    if (this.stompClient && this.stompClient.connected) {
      console.log('Already connected to Panic Notifications WebSocket');
      this.connectionStatus$.next(true);
      return;
    }

    const socket = new SockJS(this.wsUrl);

    this.stompClient = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        console.log('Connected to Panic Notifications WebSocket');
        this.connectionStatus$.next(true);
        this.subscribeToPanicTopics();
      },

      onStompError: (frame) => {
        console.error('STOMP error:', frame);
        this.connectionStatus$.next(false);
      },

      onWebSocketClose: () => {
        console.log('WebSocket connection closed');
        this.connectionStatus$.next(false);
      },

      onDisconnect: () => {
        console.log('Disconnected from WebSocket');
        this.connectionStatus$.next(false);
      }
    });

    this.stompClient.activate();
  }

  private subscribeToPanicTopics(): void {
    if (!this.stompClient) {
      console.error('STOMP client not initialized');
      return;
    }

    // Subscribe to /topic/panic
    this.panicSubscription = this.stompClient.subscribe('/topic/panic', (message) => {
      try {
        const panicDTO: PanicNotificationDTO = JSON.parse(message.body);
        console.log('New panic notification received:', panicDTO);
        this.panicNotifications$.next(panicDTO);
      } catch (error) {
        console.error('Error parsing panic notification:', error);
      }
    });

    // Subscribe to /topic/panic/resolved
    this.resolvedSubscription = this.stompClient.subscribe('/topic/panic/resolved', (message) => {
      try {
        const panicDTO: PanicNotificationDTO = JSON.parse(message.body);
        console.log('Panic resolved notification received:', panicDTO);
        this.panicResolvedNotifications$.next(panicDTO);
      } catch (error) {
        console.error('Error parsing panic resolved notification:', error);
      }
    });

    console.log('Subscribed to /topic/panic and /topic/panic/resolved');
  }

  getPanicNotificationsStream(): Observable<PanicNotificationDTO> {
    return this.panicNotifications$.asObservable();
  }

  getPanicResolvedNotificationsStream(): Observable<PanicNotificationDTO> {
    return this.panicResolvedNotifications$.asObservable();
  }

  getConnectionStatus(): Observable<boolean> {
    return this.connectionStatus$.asObservable();
  }

  isConnected(): boolean {
    return this.stompClient?.connected ?? false;
  }

  disconnectFromWebSocket(): void {
    if (this.panicSubscription) {
      this.panicSubscription.unsubscribe();
      this.panicSubscription = null;
    }

    if (this.resolvedSubscription) {
      this.resolvedSubscription.unsubscribe();
      this.resolvedSubscription = null;
    }

    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
    }

    this.connectionStatus$.next(false);
    console.log('Disconnected from Panic Notifications WebSocket');
  }

  ngOnDestroy(): void {
    this.disconnectFromWebSocket();
  }
}
