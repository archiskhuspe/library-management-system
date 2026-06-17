import React, { useEffect, useState } from 'react';
import { getBooks, deleteBook as deleteBookService } from '../services/bookService';
import type { BookDto, Page } from '../types';
import { useAuth } from '../contexts/AuthContext';
// import AddBookModal from '../components/AddBookModal'; // Old modal
import AddBookForm from '../components/AddBookForm'; // New inline form
import EditBookModal from '../components/EditBookModal';
import './BooksPage.css'; // Import page-specific styles
import { toast } from 'react-toastify'; // Import toast

const BooksPage: React.FC = () => {
  const [booksPage, setBooksPage] = useState<Page<BookDto> | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [fetchError, setFetchError] = useState<string | null>(null); // Renamed from error to fetchError
  const { authState } = useAuth();
  
  // State for the new inline AddBookForm
  const [showAddBookForm, setShowAddBookForm] = useState(false);
  
  // State for EditBookModal (remains unchanged for now)
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [selectedBookForEdit, setSelectedBookForEdit] = useState<BookDto | null>(null);

  const fetchBooks = async (page: number = 0, size: number = 10) => {
    setIsLoading(true);
    setFetchError(null); // Clear previous fetch error
    try {
      const data = await getBooks(page, size);
      setBooksPage(data);
    } catch (err) {
      // console.error("Error fetching books:", err); // Optional: for more detailed logs
      const message = 'Failed to fetch books. Please try again later.';
      setFetchError(message); // Set local error for display on page
      toast.error(message); // Show toast notification
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchBooks();
  }, []);

  const handleNextPage = () => {
    if (booksPage && !booksPage.last) {
      fetchBooks(booksPage.number + 1, booksPage.size);
    }
  };

  const handlePreviousPage = () => {
    if (booksPage && !booksPage.first) {
      fetchBooks(booksPage.number - 1, booksPage.size);
    }
  };
  
  const isLibrarian = authState.roles.includes('ROLE_LIBRARIAN');

  const handleBookAdded = () => {
    fetchBooks(); // Refresh list
    setShowAddBookForm(false); // Hide form after successful addition
  };

  const handleOpenEditModal = (book: BookDto) => {
    setSelectedBookForEdit(book);
    setIsEditModalOpen(true);
  };

  // Handler to close the inline AddBookForm
  const handleCloseAddBookForm = () => {
    setShowAddBookForm(false);
  };

  const handleCloseEditModal = (event?: React.MouseEvent) => {
    if (event) {
      event.stopPropagation();
      event.preventDefault();
    }
    setIsEditModalOpen(false);
    setSelectedBookForEdit(null);
  };

  const handleBookUpdated = () => {
    fetchBooks(); // Refresh list (toast for update is in EditBookModal)
  };

  const handleDeleteBook = async (bookId: number) => {
    if (window.confirm(`Are you sure you want to delete book ID ${bookId}? This action cannot be undone.`)) {
      try {
        await deleteBookService(bookId);
        toast.success(`Book ID ${bookId} deleted successfully!`); // Success toast
        // Refresh book list, go to previous page if current page becomes empty
        if (booksPage && booksPage.content.length === 1 && booksPage.number > 0) {
            fetchBooks(booksPage.number - 1, booksPage.size);
        } else {
            fetchBooks(booksPage ? booksPage.number : 0, booksPage ? booksPage.size : 10);
        }
      } catch (err: any) {
        // console.error(`Failed to delete book ${bookId}:`, err); // Optional
        const apiErrorMessage = err.response?.data?.message || 'Failed to delete book. Please try again.';
        toast.error(apiErrorMessage); // Error toast for delete
      }
    }
  };

  if (isLoading && !booksPage) return <div className="books-page-container"><p className="loading-inline">Loading books...</p></div>;
  // Show only fetch error if it occurs on initial load and no books are loaded yet
  if (fetchError && (!booksPage || booksPage.empty)) {
    return (
        <div className="books-page-container">
            <p className="fetch-error-inline">{fetchError}</p>
        </div>
    );
  }
  
  const renderAddBookButtonOrForm = () => {
    if (!isLibrarian) return null;

    if (showAddBookForm) {
      return (
        <AddBookForm 
          onClose={handleCloseAddBookForm} 
          onBookAdded={handleBookAdded} 
        />
      );
    }
    return (
      <div className="add-book-button-container">
          <button onClick={() => setShowAddBookForm(true)} className="add-book-button"> 
          Add New Book
          </button>
      </div>
    );
  };

  return (
    <div className="books-page-container">
      <h2>Books</h2>
      {renderAddBookButtonOrForm()} {/* Render button or form based on state */}
      
      {selectedBookForEdit && (
        <EditBookModal
          isOpen={isEditModalOpen}
          onClose={handleCloseEditModal}
          onBookUpdated={handleBookUpdated}
          book={selectedBookForEdit}
        />
      )}

      {/* Display fetchError as a less intrusive message if books are already displayed but a subsequent fetch (e.g. pagination) fails */}
      {fetchError && booksPage && !booksPage.empty && (
        <p className="fetch-error-inline">
          {fetchError} (Displaying previously loaded or partial data)
        </p>
      )}

      {(!booksPage || booksPage.empty) && !fetchError && !isLoading ? (
         <p className="no-books-message">
            {isLibrarian && !showAddBookForm ? "No books found. Click \"Add New Book\" to add one." : "No books found."}
         </p>
      ) : null}

      {booksPage && !booksPage.empty && (
        <>
          <table className="books-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Title</th>
                <th>Author</th>
                <th>ISBN</th>
                <th>Published Date</th>
                {isLibrarian && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {booksPage.content.map((book) => (
                <tr key={book.id}>
                  <td>{book.id}</td>
                  <td>{book.title}</td>
                  <td>{book.author}</td>
                  <td>{book.isbn}</td>
                  <td>{book.publishedDate}</td>
                  {isLibrarian && (
                    <td className="actions-cell">
                      <button onClick={() => handleOpenEditModal(book)} className="edit-button">Edit</button>
                      <button onClick={() => handleDeleteBook(book.id)} className="delete-button">Delete</button> 
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
          <div className="pagination-controls">
            <button onClick={handlePreviousPage} disabled={booksPage.first || isLoading}>
              Previous
            </button>
            <span> Page {booksPage.number + 1} of {booksPage.totalPages} </span>
            <button onClick={handleNextPage} disabled={booksPage.last || isLoading}>
              Next
            </button>
          </div>
        </>
      )}
      {/* Show loading indicator during pagination fetches */}
      {isLoading && booksPage && !booksPage.empty && <p className="loading-inline">Loading more books...</p>}
    </div>
  );
};

export default BooksPage; 