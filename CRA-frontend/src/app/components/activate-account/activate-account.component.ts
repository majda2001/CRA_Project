import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-activate-account',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './activate-account.component.html',
  styleUrls: ['./activate-account.component.css']
})
export class ActivateAccountComponent implements OnInit {
  token = '';
  loading = false;
  success = '';
  error = '';
  showPassword = false;
  showConfirmPassword = false;

  private readonly fb = inject(FormBuilder);

  readonly form = this.fb.group({
    password: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', [Validators.required, Validators.minLength(8)]]
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
    if (!this.token) {
      this.error = "Lien invalide: token manquant.";
    }
  }

  submit(): void {
    this.error = '';
    this.success = '';

    if (!this.token) {
      this.error = "Lien invalide ou expiré.";
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const password = this.form.controls.password.value || '';
    const confirmPassword = this.form.controls.confirmPassword.value || '';

    if (password !== confirmPassword) {
      this.error = 'Les mots de passe ne correspondent pas.';
      return;
    }

    this.loading = true;
    this.authService.completeInvitation(this.token, password).subscribe({
      next: (res) => {
        this.success = res?.message || 'Mot de passe enregistre. Vous pouvez vous connecter.';
        this.loading = false;
        setTimeout(() => this.router.navigate(['/login']), 1200);
      },
      error: (err) => {
        this.error = err?.error?.message || "Impossible de finaliser l'activation.";
        this.loading = false;
      }
    });
  }

  get passwordHint(): string {
    const value = this.form.controls.password.value || '';
    if (!value) {
      return 'Utilisez 8+ caracteres avec majuscule, minuscule, chiffre et symbole.';
    }
    if (value.length < 8) {
      return 'Mot de passe trop court.';
    }
    return 'Format valide.';
  }
}
