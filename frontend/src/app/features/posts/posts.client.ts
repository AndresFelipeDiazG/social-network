import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { APP_CONFIG } from '../../core/config/app-config';
import { FeedScope, NewPost, Post, PostPage, UploadedImage } from './post.models';

@Injectable({ providedIn: 'root' })
export class PostsClient {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(APP_CONFIG).apiBaseUrl;

  list(scope: FeedScope, page: number, size: number): Observable<PostPage> {
    const params = new HttpParams()
      .set('scope', scope)
      .set('page', page)
      .set('size', size);

    return this.http.get<PostPage>(`${this.baseUrl}/posts`, { params });
  }

  /** El autor lo deduce el backend del token; aqui solo viaja el mensaje. */
  create(post: NewPost): Observable<Post> {
    return this.http.post<Post>(`${this.baseUrl}/posts`, post);
  }

  /**
   * La imagen viaja en su propia peticion y devuelve un identificador. No se
   * fija Content-Type: lo pone el navegador con el limite de partes, y ponerlo
   * a mano rompe el multipart.
   */
  uploadImage(file: File): Observable<UploadedImage> {
    const form = new FormData();
    form.append('file', file);

    return this.http.post<UploadedImage>(`${this.baseUrl}/posts/images`, form);
  }

  // Marcar y desmarcar son idempotentes en el servidor, asi que reintentar o
  // pulsar dos veces no descuadra el recuento.
  like(postId: string): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/posts/${postId}/like`, null);
  }

  unlike(postId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/posts/${postId}/like`);
  }
}
