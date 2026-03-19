import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UiFeedbackService } from '../../services/ui-feedback.service';

@Component({
  selector: 'app-toast-host',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app-toast-host.component.html',
  styleUrls: ['./app-toast-host.component.css']
})
export class AppToastHostComponent {
  constructor(public readonly ui: UiFeedbackService) {}
}
