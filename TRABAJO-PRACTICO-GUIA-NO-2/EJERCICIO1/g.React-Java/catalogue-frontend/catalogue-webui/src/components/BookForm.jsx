import React, {useEffect, useState} from "react";
import BookService from "../services/BookService";

const BookForm = ({ currentBook, refreshList, clearSelection }) => {
    const [book, setBook] = useState({
        name: "",
        isbnNumber: "",
        publishDate: "",
        price: "",
        bookType: "HARDCOVER",
    });

    useEffect(() => {
        if (currentBook) setBook(currentBook);
    }, [currentBook]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setBook({ ...book, [name]: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (book.id) {
            await BookService.updateBook(book.id, book);
        } else {
            await BookService.addBook(book);
        }
        refreshList();
        clearSelection();
        setBook({
            name: "",
            isbnNumber: "",
            publishDate: "",
            price: "",
            bookType: "HARDCOVER",
        });
    };

    return (
        <form onSubmit={handleSubmit}>
            <h3>{book.id ? "Edit Book" : "Add Book"}</h3>
            <input
                type="text"
                name="name"
                placeholder="Book Name"
                value={book.name}
                onChange={handleChange}
                required
            />
            <input
                type="text"
                name="isbnNumber"
                placeholder="ISBN Number"
                value={book.isbnNumber}
                onChange={handleChange}
                required
            />
            <input
                type="date"
                name="publishDate"
                placeholder="Publish Date"
                value={book.publishDate}
                onChange={handleChange}
                required
            />
            <input
                type="number"
                name="price"
                placeholder="Price"
                value={book.price}
                onChange={handleChange}
                required
            />
            <select
                name="bookType"
                value={book.bookType}
                onChange={handleChange}
            >
                <option value="HARDCOVER">HARDCOVER</option>
                <option value="SOFTCOPY">SOFTCOVER</option>
                <option value="EBOOK">EBOOK</option>
            </select>
            <button type="submit">{book.id ? "Update Book" : "Add Book"} Book</button>
        </form>
    );
};

export default BookForm;