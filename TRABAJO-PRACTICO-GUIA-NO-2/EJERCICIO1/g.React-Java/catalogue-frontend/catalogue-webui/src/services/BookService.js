import axios from "axios";

const API_BASE_URL = "http://localhost:8080/api/books";

class BookService {
  getAllBooks() {
    return axios.get(`${API_BASE_URL}/getAllBooks`);
  }

  addBook(book) {
    return axios.post(`${API_BASE_URL}/addBook`, book);
  }

  updateBook(bookId, book) {
    return axios.put(`${API_BASE_URL}/updateBook/${bookId}`, book);
  }

  deleteBook(bookId) {
    return axios.delete(`${API_BASE_URL}/deleteBook/${bookId}`);
  }
}

export default new BookService();