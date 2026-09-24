import React, {useState} from "react";
import BookList from "./components/BookList";
import BookForm from "./components/BookForm";

const App = () => {
  const [selectedBook, setSelectedBook] = useState(null);
  const [refresh, setRefresh] = useState(false);

  const refreshList = () => {
    setRefresh(!refresh);
  };

  return (
    <div className="App">
      <h1>Book Catalogue</h1>
      <BookForm
        currentBook={selectedBook}
        refreshList={refreshList}
        clearSelection={() => setSelectedBook(null)}
      />
      <BookList key={refresh} onEdit={setSelectedBook}/>
    </div>
  );
};

export default App;