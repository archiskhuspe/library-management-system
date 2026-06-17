import React, { useState, useEffect } from 'react';
import { addBook } from '../services/bookService';
import type { CreateBookRequestDto } from '../types';
import './AddBookForm.css'; // New CSS for inline form
import { toast } from 'react-toastify';

interface AddBookFormProps {
  // No isOpen prop needed as visibility will be controlled by parent
  onClose: () => void; // To hide the form
  onBookAdded: () => void;
}

const AddBookForm: React.FC<AddBookFormProps> = ({ onClose, onBookAdded }) => {
  const [title, setTitle] = useState('');
  const [author, setAuthor] = useState('');
  const [isbn, setIsbn] = useState('');
  const [publishedDate, setPublishedDate] = useState('');
  const [formError, setFormError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Reset form when it's re-shown (e.g. if it was previously closed)
  // This effect might need adjustment based on how parent controls visibility
  // For now, we assume it's mounted/unmounted or receives a signal to clear.
  // A simpler approach for now: parent calls a clear function or we clear on successful submit/cancel.

  const clearForm = () => {
    setTitle('');
    setAuthor('');
    setIsbn('');
    setPublishedDate('');
    setFormError(null);
    setIsSubmitting(false);
  };

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
    const bookData: CreateBookRequestDto = { 
        title: title.trim(), 
        author: author.trim(), 
        isbn: isbn.trim(), 
        publishedDate 
    };
    try {
      await addBook(bookData);
      toast.success('Book added successfully!');
      onBookAdded();
      clearForm(); // Clear form on success
      onClose(); // Close/hide the form
    } catch (err: any) {
      const apiErrorMessage = err.response?.data?.message || 'Failed to add book. Please try again.';
      toast.error(apiErrorMessage);
      setFormError(apiErrorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCancel = () => {
    clearForm();
    onClose(); // Signal parent to hide the form
  };

  return (
    // No modal overlay or modal content wrapper needed here
    // The parent component will decide where and how to display this form
    <div className="add-book-form-container">
      <h3>Add New Book</h3>
      <form onSubmit={handleSubmit} className="add-book-inline-form">
        <div className="form-group">
          <label htmlFor="add-form-title">Title:</label>
          <input type="text" id="add-form-title" value={title} onChange={(e) => setTitle(e.target.value)} required placeholder="e.g., The Great Gatsby"/>
        </div>
        <div className="form-group">
          <label htmlFor="add-form-author">Author:</label>
          <input type="text" id="add-form-author" value={author} onChange={(e) => setAuthor(e.target.value)} required placeholder="e.g., F. Scott Fitzgerald"/>
        </div>
        <div className="form-group">
          <label htmlFor="add-form-isbn">ISBN:</label>
          <input type="text" id="add-form-isbn" value={isbn} onChange={(e) => setIsbn(e.target.value)} required placeholder="e.g., 978-3-16-148410-0"/>
        </div>
        <div className="form-group">
          <label htmlFor="add-form-publishedDate">Published Date:</label>
          <input type="date" id="add-form-publishedDate" value={publishedDate} onChange={(e) => setPublishedDate(e.target.value)} required />
        </div>
        {formError && <p className="form-inline-error-message">{formError}</p>}
        <div className="form-actions">
          <button type="submit" disabled={isSubmitting} className="submit-button">
            {isSubmitting ? 'Adding...' : 'Add Book'}
          </button>
          <button type="button" onClick={handleCancel} disabled={isSubmitting} className="cancel-button">
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
};

export default AddBookForm; 