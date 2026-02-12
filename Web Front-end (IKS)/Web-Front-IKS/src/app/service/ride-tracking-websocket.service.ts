import { Injectable, OnDestroy } from '@angular/core';
import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';

export interface RideTrackingUpdate {
  rideId: number;
  vehicleId: number;
  currentLatitude: number;
  currentLongitude: number;
  status: 'ACTIVE' | 'SCHEDULED' | 'FINISHED' | 'CANCELED';
  price: number;
  startTime: string;
  estimatedEndTime: string;
  timeLeft: string;
  route: { latitude: number; longitude: number }[];
  driver: {
    id: number;
    name: string;
    phoneNumber: string;
  };
}

@Injectable({
  providedIn: 'root'
})

export class RideTrackingWebSocketService implements OnDestroy {
  private stompClient: Client | null = null;
  private subscription: StompSubscription | null = null;
  private rideUpdates$ = new BehaviorSubject<RideTrackingUpdate | null>(null);
  private connectionStatus$ = new BehaviorSubject<boolean>(false);

  constructor() {}

  connect(userId: number): void {
    if (this.stompClient && this.stompClient.connected) {
      console.log('Already connected to WebSocket');
      return;
    }

    const socket = new SockJS('http://localhost:8080/ws');

    this.stompClient = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        console.log('Connected to Ride Tracking WebSocket');
        this.connectionStatus$.next(true);
        this.subscribeToRideTracking(userId);
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

  private subscribeToRideTracking(userId: number): void {
    if (!this.stompClient) {
      console.error('STOMP client not initialized');
      return;
    }

    this.subscription = this.stompClient.subscribe(
      `/user/${userId}/queue/ride-tracking`,
      (message) => {
        try {
          const rideUpdate: RideTrackingUpdate = JSON.parse(message.body);
          console.log('Ride update received:', rideUpdate);
          this.rideUpdates$.next(rideUpdate);
        } catch (error) {
          console.error('Error parsing ride update:', error);
        }
      }
    );

    this.stompClient.publish({
      destination: '/app/ride-tracking/subscribe',
      body: userId.toString()
    });

    console.log(`Subscribed to ride tracking for user ${userId}`);
  }

  getRideUpdates(): Observable<RideTrackingUpdate | null> {
    return this.rideUpdates$.asObservable();
  }

  getConnectionStatus(): Observable<boolean> {
    return this.connectionStatus$.asObservable();
  }

  getCurrentRideUpdate(): RideTrackingUpdate | null {
    return this.rideUpdates$.value;
  }

  isConnected(): boolean {
    return this.stompClient?.connected ?? false;
  }

  disconnect(userId?: number): void {
    if (this.stompClient && this.stompClient.connected && userId) {
      this.stompClient.publish({
        destination: '/app/ride-tracking/unsubscribe',
        body: userId.toString()
      });
    }

    if (this.subscription) {
      this.subscription.unsubscribe();
      this.subscription = null;
    }

    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
    }

    this.rideUpdates$.next(null);
    this.connectionStatus$.next(false);
    console.log('Disconnected from Ride Tracking WebSocket');
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}