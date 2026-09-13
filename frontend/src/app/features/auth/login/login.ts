import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthStore } from '../../../core/auth/auth.store';
import { APP_CONFIG } from '../../../core/config/app-config';
import { Brand } from '../../../shared/brand';
import { ThemeToggle } from '../../../shared/theme-toggle';

const FEED = '/muro';

/** Los que siembra Flyway al arrancar. Estan a la vista para no tener que abrir
 *  el script de base de datos cada vez que se prueba la aplicacion. */
const DEMO_PASSWORD = 'Password123!';
const DEMO_USERNAME = 'acorrea';

const OTHER_USERS = ['jmendoza', 'lvargas', 'dcastillo'] as const;

@Component({
  selector: 'app-login',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Brand, ReactiveFormsModule, ThemeToggle],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class LoginPage {

  private readonly auth = inject(AuthStore);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly appName = inject(APP_CONFIG).appName;

  protected readonly form = inject(FormBuilder).nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  protected readonly passwordVisible = signal(false);

  protected readonly submitting = this.auth.submitting;
  protected readonly error = this.auth.error;
  protected readonly demoUsername = DEMO_USERNAME;
  protected readonly demoPassword = DEMO_PASSWORD;
  protected readonly otherUsers = OTHER_USERS;

  /** Solo se marca el error cuando el campo ya se ha tocado o enviado. */
  protected invalid(field: 'username' | 'password'): boolean {
    const control = this.form.controls[field];
    return control.invalid && (control.touched || control.dirty);
  }

  protected togglePassword(): void {
    this.passwordVisible.update((visible) => !visible);
  }

  protected async submit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (await this.auth.login(this.form.getRawValue())) {
      await this.router.navigateByUrl(this.destination());
    }
  }

  /** Rellena las dos casillas: los cuatro usuarios comparten contrasena. */
  protected useDemoUser(username: string): void {
    this.form.setValue({ username, password: DEMO_PASSWORD });
    this.auth.dismissError();
  }

  /**
   * El destino viene de la URL, asi que lo elige quien envia el enlace. Solo se
   * aceptan rutas internas: '//otro-dominio' es una ruta valida para el router
   * y el navegador la resolveria como host externo.
   */
  private destination(): string {
    const requested = this.route.snapshot.queryParamMap.get('volverA');
    const internal = requested !== null && requested.startsWith('/') && !requested.startsWith('//');

    return internal ? requested : FEED;
  }
}
