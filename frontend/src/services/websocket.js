import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
  constructor() {
    this.client = null;
    this.connected = false;
    this.subscriptions = new Map();
  }

  connect() {
    return new Promise((resolve, reject) => {
      try {
        this.client = new Client({
          webSocketFactory: () => new SockJS('/ws-stocks'),
          onConnect: () => {
            console.log('WebSocket connected');
            this.connected = true;
            resolve();
          },
          onDisconnect: () => {
            console.log('WebSocket disconnected');
            this.connected = false;
          },
          onStompError: frame => {
            console.error('STOMP error:', frame);
            reject(frame);
          },
        });

        this.client.activate();
      } catch (error) {
        reject(error);
      }
    });
  }

  disconnect() {
    if (this.client && this.connected) {
      this.client.deactivate();
      this.subscriptions.clear();
    }
  }

  subscribeToQuotes(symbol, callback) {
    if (!this.connected) return null;

    const destination = `/topic/quotes/${symbol}`;
    const subscription = this.client.subscribe(destination, message => {
      const quote = JSON.parse(message.body);
      callback(quote);
    });

    this.subscriptions.set(`quotes-${symbol}`, subscription);

    // Send subscription message to backend
    this.client.publish({
      destination: '/app/subscribe',
      body: symbol,
    });

    return subscription;
  }

  subscribeToIndicators(symbol, callback) {
    if (!this.connected) return null;

    const destination = `/topic/indicators/${symbol}`;
    const subscription = this.client.subscribe(destination, message => {
      const indicators = JSON.parse(message.body);
      callback(indicators);
    });

    this.subscriptions.set(`indicators-${symbol}`, subscription);
    return subscription;
  }

  subscribeToSignals(symbol, callback) {
    if (!this.connected) return null;

    const destination = `/topic/signals/${symbol}`;
    const subscription = this.client.subscribe(destination, message => {
      const signal = JSON.parse(message.body);
      callback(signal);
    });

    this.subscriptions.set(`signals-${symbol}`, subscription);
    return subscription;
  }

  unsubscribeFromSymbol(symbol) {
    const subscriptionKeys = [`quotes-${symbol}`, `indicators-${symbol}`, `signals-${symbol}`];

    subscriptionKeys.forEach(key => {
      const subscription = this.subscriptions.get(key);
      if (subscription) {
        subscription.unsubscribe();
        this.subscriptions.delete(key);
      }
    });

    // Send unsubscription message to backend
    if (this.connected) {
      this.client.publish({
        destination: '/app/unsubscribe',
        body: symbol,
      });
    }
  }

  isConnected() {
    return this.connected;
  }
}

export default new WebSocketService();
