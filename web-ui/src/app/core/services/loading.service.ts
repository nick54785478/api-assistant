import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LoadingService {
  private _isLoading = signal<boolean>(false);

  // Expose as readonly signal for templates to subscribe to
  public readonly isLoading = this._isLoading.asReadonly();

  show(): void {
    this._isLoading.set(true);
  }

  hide(): void {
    this._isLoading.set(false);
  }
}
