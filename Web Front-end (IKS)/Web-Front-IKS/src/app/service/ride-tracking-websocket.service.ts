import { Injectable, OnDestroy } from '@angular/core';
import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';
import { RideTrackingUpdate } from '../models/ride-tracking.models';

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

      onStompError: (frame: any) => {
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
      (message: { body: string; }) => {
        try {
          const data = JSON.parse(message.body);
          
          // Handle both single ride object and array of rides
          let rideUpdate: RideTrackingUpdate | null = null;
          
          if (Array.isArray(data)) {
            // If backend sends array of rides, prioritize: ACTIVE > SCHEDULED > FINISHED
            rideUpdate = this.selectPrioritizedRide(data);
          } else {
            // If backend sends single ride object
            rideUpdate = data as RideTrackingUpdate;
          }
          
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

  /**
   * Selects the highest priority ride from an array
   * Priority: ACTIVE > SCHEDULED > FINISHED
   */
  private selectPrioritizedRide(rides: RideTrackingUpdate[]): RideTrackingUpdate | null {
    // Look for ACTIVE ride first
    const activeRide = rides.find(r => r.status === 'ACTIVE');
    if (activeRide) {
      return activeRide;
    }

    // Fall back to SCHEDULED ride
    const scheduledRide = rides.find(r => r.status === 'SCHEDULED');
    if (scheduledRide) {
      return scheduledRide;
    }

    // Fall back to FINISHED ride (for rating)
    const finishedRide = rides.find(r => r.status === 'FINISHED');
    if (finishedRide) {
      return finishedRide;
    }

    // Return first ride if none of the above
    return rides.length > 0 ? rides[0] : null;
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