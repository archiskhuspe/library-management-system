import apiClient from './api';
import type { BookDto, CreateBookRequestDto, UpdateBookRequestDto, Page } from '../types';

export const getBooks = async (page: number = 0, size: number = 10, sort: string = 'title,asc'): Promise<Page<BookDto>> => {
  try {
    const response = await apiClient.get<Page<BookDto>>('/books', {
      params: { page, size, sort }
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching books:', error);
    throw error;
  }
};

export const getBookById = async (id: number): Promise<BookDto> => {
    try {
        const response = await apiClient.get<BookDto>(`/books/${id}`);
        return response.data;
    } catch (error) {
        console.error(`Error fetching book with id ${id}:`, error);
        throw error;
    }
};

export const addBook = async (bookData: CreateBookRequestDto): Promise<BookDto> => {
  try {
    const response = await apiClient.post<BookDto>('/books', bookData);
    return response.data;
  } catch (error) {
    console.error('Error adding book:', error);
    throw error;
  }
};

export const updateBook = async (id: number, bookData: UpdateBookRequestDto): Promise<BookDto> => {
    try {
        const response = await apiClient.put<BookDto>(`/books/${id}`, bookData);
        return response.data;
    } catch (error) {
        console.error(`Error updating book with id ${id}:`, error);
        throw error;
    }
};

export const deleteBook = async (id: number): Promise<void> => {
    try {
        await apiClient.delete(`/books/${id}`);
    } catch (error) {
        console.error(`Error deleting book with id ${id}:`, error);
        throw error;
    }
}; 