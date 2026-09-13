import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Post } from '../post.models';
import { PostCard } from './post-card';

function post(overrides: Partial<Post> = {}): Post {
  return {
    id: '1',
    message: 'Migraciones versionadas desde el primer dia.',
    publishedAt: new Date(Date.now() - 4 * 60 * 1000).toISOString(),
    author: { id: 'a1', username: 'camiloh', displayName: 'Camilo Herrera' },
    mine: false,
    likes: 0,
    likedByMe: false,
    imageUrl: null,
    ...overrides,
  };
}

describe('PostCard', () => {
  let fixture: ComponentFixture<PostCard>;

  async function render(value: Post): Promise<void> {
    fixture = TestBed.createComponent(PostCard);
    fixture.componentRef.setInput('post', value);
    await fixture.whenStable();
  }

  function textOf(selector: string): string {
    const element = (fixture.nativeElement as HTMLElement).querySelector(selector);
    return element?.textContent?.replace(/\s+/g, ' ').trim() ?? '';
  }

  it('separa el nombre del usuario y la fecha en dos lineas', async () => {
    await render(post());

    expect(textOf('.post__name')).toBe('Camilo Herrera');
    expect(textOf('.post__meta')).toBe('@camiloh · hace 4 min');
  });

  it('el mensaje va fuera de la cabecera, a ancho completo', async () => {
    await render(post());

    const message = (fixture.nativeElement as HTMLElement).querySelector('.post__message');

    expect(message?.textContent).toContain('Migraciones versionadas');
    expect(message?.closest('.post__head')).toBeNull();
  });

  it('marca las publicaciones propias', async () => {
    await render(post({ mine: true }));

    expect(textOf('.post__badge')).toBe('Tú');
  });

  it('las ajenas no llevan marca', async () => {
    await render(post());

    expect((fixture.nativeElement as HTMLElement).querySelector('.post__badge')).toBeNull();
  });

  it('la hora exacta queda en el title, que el relativo la pierde', async () => {
    await render(post({ publishedAt: '2026-09-13T10:00:00Z' }));

    const time = (fixture.nativeElement as HTMLElement).querySelector('time');

    expect(time?.getAttribute('datetime')).toBe('2026-09-13T10:00:00Z');
    expect(time?.getAttribute('title')).toContain('2026');
  });
});
