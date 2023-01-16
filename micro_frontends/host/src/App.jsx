import React from "react";
import ReactDOM from "react-dom";
 
import "./index.css";

const Counter =
  React.lazy(() =>
    import('mf1/Counter')
  );
const App = () => (
  <div className="container">
    <div>Name: host</div>
    <React.Suspense fallback='Loading Button'>
    <Counter></Counter>
    </React.Suspense>
   
    </div>
);
ReactDOM.render(<App />, document.getElementById("app"));
