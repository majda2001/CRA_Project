import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';

export type ToastType = 'success' | 'error' | 'info' | 'warning';

export interface ToastMessage {
  id: number;
  type: ToastType;
  message: string;
}

export interface ConfirmOptions {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
}

interface ConfirmState extends ConfirmOptions {
  isOpen: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class UiFeedbackService {
  private nextToastId = 1;
  private readonly toastsSubject = new BehaviorSubject<ToastMessage[]>([]);
  readonly toasts$ = this.toastsSubject.asObservable();

  private readonly confirmStateSubject = new BehaviorSubject<ConfirmState | null>(null);
  readonly confirmState$ = this.confirmStateSubject.asObservable();

  private confirmResolver: ((value: boolean) => void) | null = null;

  showToast(type: ToastType, message: string, durationMs = 2800): void {
    const toast: ToastMessage = {
      id: this.nextToastId++,
      type,
      message
    };

    this.toastsSubject.next([...this.toastsSubject.value, toast]);

    setTimeout(() => {
      this.dismissToast(toast.id);
    }, durationMs);
  }

  dismissToast(id: number): void {
    this.toastsSubject.next(this.toastsSubject.value.filter(t => t.id !== id));
  }

  confirm(options: ConfirmOptions): Promise<boolean> {
    this.confirmStateSubject.next({
      isOpen: true,
      ...options,
      confirmLabel: options.confirmLabel || 'Confirmer',
      cancelLabel: options.cancelLabel || 'Annuler'
    });

    return new Promise<boolean>(resolve => {
      this.confirmResolver = resolve;
    });
  }

  resolveConfirm(confirmed: boolean): void {
    if (this.confirmResolver) {
      this.confirmResolver(confirmed);
      this.confirmResolver = null;
    }
    this.confirmStateSubject.next(null);
  }
}
