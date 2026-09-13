export type FeedScope = 'OTHERS' | 'ALL' | 'MINE';

export interface PostAuthor {
  readonly id: string;
  readonly username: string;
  readonly displayName: string;
}

export interface Post {
  readonly id: string;
  readonly message: string;
  readonly publishedAt: string;
  readonly author: PostAuthor;
  readonly mine: boolean;
  readonly likes: number;
  readonly likedByMe: boolean;
  readonly imageUrl: string | null;
}

export interface PostPage {
  readonly content: Post[];
  readonly page: number;
  readonly size: number;
  readonly totalElements: number;
  readonly totalPages: number;
}

export interface NewPost {
  readonly message: string;
  readonly imageId?: string;
}

export interface UploadedImage {
  readonly id: string;
  readonly url: string;
}

/** Lo que admite el backend, comprobado alli por los bytes y no por la extension. */
export const ACCEPTED_IMAGE_TYPES = 'image/jpeg,image/png,image/gif,image/webp';
export const MAX_IMAGE_BYTES = 2 * 1024 * 1024;

export const MAX_MESSAGE_LENGTH = 280;
