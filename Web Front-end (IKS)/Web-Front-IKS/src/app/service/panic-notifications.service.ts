import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, OnDestroy, inject } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { PanicNotificationDTO, PageResponse } from '../models/panic.models';
import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { MatSnackBar } from '@angular/material/snack-bar';

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
  
  private snackBar = inject(MatSnackBar);
  private audioContext?: AudioContext;
  private notificationsInitialized = false;

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
        
        // Display notification globally
        this.displayPanicNotification(panicDTO);
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
        
        // Display notification globally
        this.displayResolvedNotification(panicDTO);
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

  initializeGlobalNotifications(): void {
    if (this.notificationsInitialized) {
      return;
    }
    this.notificationsInitialized = true;
    
    // Request notification permission for browser notifications
    if ('Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission();
    }
  }

  private displayPanicNotification(panic: PanicNotificationDTO): void {
    // Show browser notification
    this.showBrowserNotification(
      'PANIC ALERT!',
      `Panic from ${panic.callerName} (ID: ${panic.callerId}) - Ride #${panic.rideId}`,
      'urgent'
    );

    // Play urgent audio
    this.playNotificationSound('urgent');

    // Show snackbar
    this.snackBar.open(
      `🚨 NEW PANIC: ${panic.callerName} - Ride #${panic.rideId}`,
      'View',
      { 
        duration: 10000,
        panelClass: ['panic-snackbar']
      }
    );
  }

  private displayResolvedNotification(panic: PanicNotificationDTO): void {
    // Show browser notification
    this.showBrowserNotification(
      'Panic Resolved',
      `Panic #${panic.id} from ${panic.callerName} has been resolved`,
      'resolved'
    );

    // Play resolved audio
    this.playNotificationSound('resolved');

    // Show snackbar
    this.snackBar.open(
      `✓ Panic #${panic.id} resolved - ${panic.callerName}`,
      'OK',
      { 
        duration: 5000,
        panelClass: ['resolved-snackbar']
      }
    );
  }

  private showBrowserNotification(title: string, body: string, type: 'urgent' | 'resolved'): void {
    if ('Notification' in window && Notification.permission === 'granted') {
      const notification = new Notification(title, {
        body: body,
        icon: type === 'urgent' ? '/assets/panic-icon.png' : '/assets/check-icon.png',
        badge: '/assets/panic-icon.png',
        tag: 'panic-notification',
        requireInteraction: type === 'urgent', // Keep urgent notifications visible
        silent: false
      });

      notification.onclick = () => {
        window.focus();
        notification.close();
      };
    }
  }

  private playNotificationSound(type: 'urgent' | 'resolved'): void {
    try {
      // Create audio context if not exists
      if (!this.audioContext) {
        this.audioContext = new (window.AudioContext || (window as any).webkitAudioContext)();
      }

      const ctx = this.audioContext;
      const oscillator = ctx.createOscillator();
      const gainNode = ctx.createGain();

      oscillator.connect(gainNode);
      gainNode.connect(ctx.destination);

      if (type === 'urgent') {
        // Urgent alarm sound: alternating high-pitched beeps
        oscillator.frequency.setValueAtTime(880, ctx.currentTime); // A5 note
        oscillator.frequency.setValueAtTime(1046, ctx.currentTime + 0.15); // C6 note
        oscillator.frequency.setValueAtTime(880, ctx.currentTime + 0.3);
        oscillator.frequency.setValueAtTime(1046, ctx.currentTime + 0.45);
        
        gainNode.gain.setValueAtTime(0.3, ctx.currentTime);
        gainNode.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.6);
        
        oscillator.start(ctx.currentTime);
        oscillator.stop(ctx.currentTime + 0.6);
      } else {
        // Resolved sound: pleasant two-tone chime
        oscillator.frequency.setValueAtTime(523, ctx.currentTime); // C5 note
        oscillator.frequency.setValueAtTime(659, ctx.currentTime + 0.15); // E5 note
        
        gainNode.gain.setValueAtTime(0.2, ctx.currentTime);
        gainNode.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.4);
        
        oscillator.start(ctx.currentTime);
        oscillator.stop(ctx.currentTime + 0.4);
      }
    } catch (error) {
      console.error('Error playing notification sound:', error);
    }
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
