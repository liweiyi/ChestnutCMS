// 一个简单的事件订阅发布的实现

export class Event {
  triggered = false;
  requested = false;
  callbacks = [];
}

export class LoadEvent {

  listeners;

  constructor() {
    this.listeners = new Map();
  }

  getListener(eventName) {
    return this.listeners.get(eventName);
  }

  on(eventName, callback) {
    if (!this.listeners.has(eventName)) {
      this.listeners.set(eventName, new Event());
    }
    // 如果已经触发过，后续添加监听的 callback 会被直接执行
    if (this.listeners.get(eventName).triggered) {
      callback();
    }
    this.listeners.get(eventName).callbacks.push(callback);
  }

  emit(eventName) {
    if (this.listeners.has(eventName)) {
      this.listeners.get(eventName).triggered = true;
      this.listeners.get(eventName).callbacks.forEach((callback) => callback());
    }
  }
}