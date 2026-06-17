export interface BookDto {
  id: number;
  title: string;
  author: string;
  isbn: string;
  publishedDate: string; // Consider formatting as YYYY-MM-DD
}

export interface CreateBookRequestDto {
  title: string;
  author: string;
  isbn: string;
  publishedDate: string; // Expecting YYYY-MM-DD
}

export interface UpdateBookRequestDto {
  title?: string;
  author?: string;
  isbn?: string; 
  publishedDate?: string;
}

export interface Page<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number; 
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  first: boolean;
  numberOfElements: number; 
  empty: boolean;
} 