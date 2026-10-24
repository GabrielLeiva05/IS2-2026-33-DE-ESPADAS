import React, { useEffect, useState } from "react";
import BookService from "../services/BookService";

const BookList = ({ onEdit }) => {
    const [books, setBooks] = useState([]);

    const fetchBooks = async () => {
        const response = await BookService.getAllBooks();
        setBooks(response.data);
    };

    useEffect(() => {
        fetchBooks();
    }, []);

    const handleDelete = async (bookId) => {
        await BookService.deleteBook(bookId);
        fetchBooks();
    }

    return (
        <div className="container">
            <h2>Book Catalogue</h2>
            <table border="1" cellPadding="8" cellSpacing="0"> 
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>ISBN Number</th>
                        <th>Publish Date</th>
                        <th>Price</th>
                        <th>Book Type</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {books.map((book) => (
                        <tr key={book.id}>
                            <td>{book.id}</td>
                            <td>{book.name}</td>
                            <td>{book.isbnNumber}</td>
                            <td>{book.publishDate}</td>
                            <td>${book.price}</td>
                            <td>{book.bookType}</td>
                            <td>
                                <button onClick={() => onEdit(book)}>Edit</button>
                                <button onClick={() => handleDelete(book.id)}>Delete</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default BookList;