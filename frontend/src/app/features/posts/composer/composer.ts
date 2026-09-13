import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  computed,
  inject,
  input,
  signal,
  viewChild,
} from '@angular/core';
import { Avatar } from '../../../shared/avatar';
import { EmojiPicker } from '../../../shared/emoji-picker';
import { ACCEPTED_IMAGE_TYPES, MAX_IMAGE_BYTES, MAX_MESSAGE_LENGTH } from '../post.models';
import { PostsStore } from '../posts.store';

/** A partir de aqui el contador se muestra en color de aviso. */
const WARNING_THRESHOLD = 40;

@Component({
  selector: 'app-composer',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Avatar, EmojiPicker],
  templateUrl: './composer.html',
  styleUrl: './composer.scss',
})
export class Composer {

  readonly author = input.required<string>();

  private readonly posts = inject(PostsStore);
  private readonly area = viewChild.required<ElementRef<HTMLTextAreaElement>>('area');

  protected readonly message = signal('');
  protected readonly image = signal<File | null>(null);
  protected readonly imagePreview = signal<string | null>(null);
  protected readonly imageError = signal<string | null>(null);
  protected readonly acceptedTypes = ACCEPTED_IMAGE_TYPES;
  protected readonly confirmed = signal(false);
  protected readonly publishing = this.posts.publishing;
  protected readonly maxLength = MAX_MESSAGE_LENGTH;

  private readonly trimmed = computed(() => this.message().trim());

  protected readonly remaining = computed(() => MAX_MESSAGE_LENGTH - this.trimmed().length);
  protected readonly nearLimit = computed(() => this.remaining() <= WARNING_THRESHOLD);
  // Con imagen se puede publicar sin texto: la foto ya es el contenido.
  protected readonly canPublish = computed(
    () => (this.trimmed().length > 0 || this.image() !== null)
      && this.remaining() >= 0
      && !this.publishing(),
  );

  protected write(value: string): void {
    this.message.set(value);
    this.confirmed.set(false);
  }

  /**
   * Inserta donde este el cursor, no al final: si alguien vuelve a media frase
   * para poner una cara, esperaria verla ahi. Se escribe tambien en el elemento
   * para poder recolocar el cursor sin esperar al siguiente ciclo de pintado.
   */
  protected insertEmoji(emoji: string): void {
    const element = this.area().nativeElement;
    const current = this.message();
    const from = element.selectionStart ?? current.length;
    const to = element.selectionEnd ?? from;
    const next = current.slice(0, from) + emoji + current.slice(to);
    const caret = from + emoji.length;

    this.write(next);
    element.value = next;
    element.focus();
    element.setSelectionRange(caret, caret);
  }

  /**
   * Se valida el tamano aqui ademas de en el servidor: subir dos megabytes para
   * que los rechacen es tiempo y datos del usuario tirados.
   */
  protected chooseImage(input: HTMLInputElement): void {
    const file = input.files?.[0] ?? null;
    input.value = '';

    if (file === null) {
      return;
    }

    if (file.size > MAX_IMAGE_BYTES) {
      this.imageError.set('La imagen no puede superar los 2 MB');
      return;
    }

    this.clearImage();
    this.image.set(file);
    this.imagePreview.set(URL.createObjectURL(file));
    this.confirmed.set(false);
  }

  protected clearImage(): void {
    const preview = this.imagePreview();

    // Cada createObjectURL retiene el archivo hasta que se revoca.
    if (preview !== null) {
      URL.revokeObjectURL(preview);
    }

    this.image.set(null);
    this.imagePreview.set(null);
    this.imageError.set(null);
  }

  protected async submit(): Promise<void> {
    if (!this.canPublish()) {
      return;
    }

    // La fecha no se envia: la pone el servidor en el momento de guardar.
    if (await this.posts.publish(this.trimmed(), this.image())) {
      this.message.set('');
      this.clearImage();
      this.confirmed.set(true);
    }
  }

  /**
   * Ctrl+Enter publica. Enter a secas inserta un salto de linea, que es lo que
   * se espera de un area de texto de varias lineas.
   */
  protected onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) {
      event.preventDefault();
      void this.submit();
    }
  }
}
