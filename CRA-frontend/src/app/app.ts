import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AppToastHostComponent } from './components/shared/app-toast-host.component';
import { AppConfirmDialogComponent } from './components/shared/app-confirm-dialog.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, AppToastHostComponent, AppConfirmDialogComponent],
  templateUrl: './app.html'
})
export class App {
  protected readonly title = signal('CRA-frontend');
}
