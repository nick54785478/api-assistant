import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class StorageService {
  
  private isBrowser(): boolean {
    return typeof window !== 'undefined';
  }

  setItem(key: string, value: string, storageType: 'local' | 'session' = 'local'): void {
    if (this.isBrowser()) {
      const storage = storageType === 'local' ? window.localStorage : window.sessionStorage;
      storage.setItem(key, value);
    }
  }

  getItem(key: string, storageType: 'local' | 'session' = 'local'): string | null {
    if (this.isBrowser()) {
      const storage = storageType === 'local' ? window.localStorage : window.sessionStorage;
      return storage.getItem(key);
    }
    return null;
  }

  removeItem(key: string, storageType: 'local' | 'session' = 'local'): void {
    if (this.isBrowser()) {
      const storage = storageType === 'local' ? window.localStorage : window.sessionStorage;
      storage.removeItem(key);
    }
  }

  clear(storageType: 'local' | 'session' = 'local'): void {
    if (this.isBrowser()) {
      const storage = storageType === 'local' ? window.localStorage : window.sessionStorage;
      storage.clear();
    }
  }
}
