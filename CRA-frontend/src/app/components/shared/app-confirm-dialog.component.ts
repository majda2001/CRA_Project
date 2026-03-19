import { Component, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UiFeedbackService } from '../../services/ui-feedback.service';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app-confirm-dialog.component.html',
  styleUrls: ['./app-confirm-dialog.component.css']
})
export class AppConfirmDialogComponent {
  constructor(public readonly ui: UiFeedbackService) {}

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.cancel();
  }

  @HostListener('document:keydown.enter')
  onEnter(): void {
    this.confirm();
  }

  cancel(): void {
    this.ui.resolveConfirm(false);
  }

  confirm(): void {
    this.ui.resolveConfirm(true);
  }
}
