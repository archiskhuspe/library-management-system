import React, { useState, useEffect } from 'react';
import { updateBook } from '../services/bookService';
import type { BookDto, UpdateBookRequestDto } from '../types';
import './EditBookModal.css'; // Import new modal styles
import { toast } from 'react-toastify'; // Import toast

interface EditBookModalProps {
  isOpen: boolean;
  onClose: () => void;
  onBookUpdated: () => void;
  book: BookDto | null; 
}

const EditBookModal: React.FC<EditBookModalProps> = ({ isOpen, onClose, onBookUpdated, book }) => {
  const [title, setTitle] = useState('');
  const [author, setAuthor] = useState('');
  const [isbn, setIsbn] = useState('');
  const [publishedDate, setPublishedDate] = useState('');
  const [formError, setFormError] = useState<string | null>(null); // Renamed from error to formError
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (isOpen && book) {
      setTitle(book.title);
      setAuthor(book.author);
      setIsbn(book.isbn);
      setPublishedDate(book.publishedDate); 
      setFormError(null); // Reset form error
      setIsSubmitting(false); // Reset submitting state when opening
    } else if (!isOpen) {
      setTitle('');
      setAuthor('');
      setIsbn('');
      setPublishedDate('');
      setFormError(null);
      setIsSubmitting(false);
    }
  }, [isOpen, book]);

  if (!isOpen || !book) {
    return null;
  }

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setFormError(null);

    if (title.trim() === '' || author.trim() === '' || isbn.trim() === '') {
      setFormError('Title, Author, and ISBN cannot be empty or just whitespace.');
      return;
    }
    if (!/^(?=(?:\D*\d){10}(?:(?:\D*\d){3})?$)[\d-]+$/.test(isbn.trim())) {
        setFormError('Please enter a valid ISBN (e.g., 10 or 13 digits, hyphens allowed).');
        return;
    }
    if (!publishedDate) {
        setFormError('Published Date is required.');
        return;
    }

    setIsSubmitting(true);

    const updatedBookData: UpdateBookRequestDto = { 
        title: title.trim() !== book.title ? title.trim() : undefined,
        author: author.trim() !== book.author ? author.trim() : undefined,
        isbn: isbn.trim() !== book.isbn ? isbn.trim() : undefined, 
        publishedDate: publishedDate !== book.publishedDate ? publishedDate : undefined,
    };
    
    const changedData = Object.entries(updatedBookData)
        .filter(([_, value]) => value !== undefined)
        .reduce((obj, [key, value]) => {
            (obj as any)[key] = value;
            return obj;
        }, {} as UpdateBookRequestDto);

    if (Object.keys(changedData).length === 0) {
        // Using toast.info for non-critical messages
        toast.info("No changes detected to update."); 
        setIsSubmitting(false); 
        onClose(); 
        return;
    }

    try {
      await updateBook(book.id, changedData);
      toast.success('Book updated successfully!'); // Success toast
      onBookUpdated();
      onClose();
    } catch (err: any) {
      // console.error(`Failed to update book ${book.id}:`, err); // Keep or remove
      const apiErrorMessage = err.response?.data?.message || 'Failed to update book. Please try again.';
      toast.error(apiErrorMessage); // Error toast
      setFormError(apiErrorMessage); // Optionally set local form error
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      <div className="edit-book-modal-overlay" onClick={onClose} />
      <div className="edit-book-modal-content" onClick={(e) => e.stopPropagation()}>
        <h2>Edit Book (ID: {book.id})</h2>
        <form onSubmit={handleSubmit} className="edit-book-form">
          <div className="form-group">
            <label htmlFor="edit-title">Title:</label>
            <input type="text" id="edit-title" value={title} onChange={(e) => setTitle(e.target.value)} required placeholder="e.g., The Great Gatsby"/>
          </div>
          <div className="form-group">
            <label htmlFor="edit-author">Author:</label>
            <input type="text" id="edit-author" value={author} onChange={(e) => setAuthor(e.target.value)} required placeholder="e.g., F. Scott Fitzgerald"/>
          </div>
          <div className="form-group">
            <label htmlFor="edit-isbn">ISBN:</label>
            <input type="text" id="edit-isbn" value={isbn} onChange={(e) => setIsbn(e.target.value)} required placeholder="e.g., 978-3-16-148410-0"/>
          </div>
          <div className="form-group">
            <label htmlFor="edit-publishedDate">Published Date (YYYY-MM-DD):</label>
            <input type="date" id="edit-publishedDate" value={publishedDate} onChange={(e) => setPublishedDate(e.target.value)} required />
          </div>
          {formError && <p className="modal-error-message">{formError}</p>} {/* Display formError */}
          <div className="form-actions">
            <button type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Updating...' : 'Update Book'}
            </button>
            <button type="button" onClick={onClose} disabled={isSubmitting}>
              Cancel
            </button>
          </div>
        </form>
      </div>
    </>
  );
};

export default EditBookModal; 