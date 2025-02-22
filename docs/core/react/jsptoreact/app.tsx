import React from 'react';
import ReactDOM from 'react-dom';
import MyReactPage from './MyReactPage';

// Main application entry point
const App = () => {
  return (
    <div className="app-container">
      <MyReactPage />
    </div>
  );
};

// Mount the React application to replace your JSP content
const rootElement = document.getElementById('react-root');
ReactDOM.render(<App />, rootElement);

// Alternatively, if you're migrating gradually and want to 
// mount React components alongside existing JSP content:
document.querySelectorAll('.react-mount-point').forEach(element => {
  // Get any data attributes from the element if needed
  const url = element.dataset.url;
  const params = JSON.parse(element.dataset.params || '{}');
  
  ReactDOM.render(
    <AjaxContent url={url} params={params} />,
    element
  );
});