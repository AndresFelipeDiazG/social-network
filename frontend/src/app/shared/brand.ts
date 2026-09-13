import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-brand',
  changeDetection: ChangeDetectionStrategy.OnPush,
  // La clase va en el anfitrion porque la encapsulacion de estilos impide que el
  // componente padre alcance nada de dentro.
  host: { '[class.brand--keepName]': 'keepName()' },
  template: `
    <span class="brand">
      <span class="brand__mark" aria-hidden="true">
        <svg viewBox="0 0 24 24">
          <path d="M4.5 6.6A2.1 2.1 0 0 1 6.6 4.5h10.8a2.1 2.1 0 0 1 2.1 2.1v6.9a2.1 2.1 0 0 1-2.1 2.1H10l-4 3.4a.6.6 0 0 1-1-.46V15.6H6.6a2.1 2.1 0 0 1-2.1-2.1Z" />
        </svg>
      </span>
      <span class="brand__name">{{ name() }}</span>
    </span>
  `,
  styles: `
    .brand {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .brand__mark {
      display: grid;
      place-items: center;
      flex: 0 0 auto;
      width: 34px;
      height: 34px;
      border-radius: 10px;
      background: var(--accent);
    }

    .brand__mark svg {
      width: 19px;
      height: 19px;
      fill: #fff;
    }

    .brand__name {
      font-size: 1.0625rem;
      font-weight: 700;
      letter-spacing: -0.02em;
      white-space: nowrap;
    }

    /* En una barra estrecha el nombre estorba y basta el simbolo, salvo que quien
       lo usa pida conservarlo. */
    @media (max-width: 560px) {
      :host(:not(.brand--keepName)) .brand__name {
        display: none;
      }
    }
  `,
})
export class Brand {

  readonly name = input.required<string>();

  /** Mantiene el nombre visible tambien en pantalla estrecha. */
  readonly keepName = input(false);
}
