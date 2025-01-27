# React Mastery Guide - From Beginner to Advanced

## Beginner Level Features

### 1. JSX and Components
```jsx
// Basic component structure
function Welcome() {
  return (
    <div>
      <h1>Hello, React!</h1>
      <p>Welcome to my first component</p>
    </div>
  );
}
```

Key concepts:
- JSX syntax and rules
- Component creation
- Component composition
- Props passing
- Basic styling

### 2. State Management with useState
```jsx
function Counter() {
  const [count, setCount] = useState(0);
  
  return (
    <div>
      <p>Count: {count}</p>
      <button onClick={() => setCount(count + 1)}>
        Increment
      </button>
    </div>
  );
}
```

Key concepts:
- State initialization
- State updates
- Multiple state variables
- State with objects

### 3. Event Handling
```jsx
function Form() {
  const handleSubmit = (e) => {
    e.preventDefault();
    // Form handling logic
  };

  return (
    <form onSubmit={handleSubmit}>
      <input onChange={(e) => console.log(e.target.value)} />
      <button type="submit">Submit</button>
    </form>
  );
}
```

Key concepts:
- Event types
- Event handlers
- Event objects
- Preventing default behavior

### 4. Conditional Rendering
```jsx
function Greeting({ isLoggedIn }) {
  return (
    <div>
      {isLoggedIn ? (
        <h1>Welcome back!</h1>
      ) : (
        <h1>Please sign in</h1>
      )}
    </div>
  );
}
```

Key concepts:
- Ternary operators
- Logical && operator
- Switch statements
- Conditional styles

### 5. Lists and Keys
```jsx
function ItemList({ items }) {
  return (
    <ul>
      {items.map((item) => (
        <li key={item.id}>{item.name}</li>
      ))}
    </ul>
  );
}
```

Key concepts:
- Map function
- Key prop importance
- Array methods
- List optimization

### 6. Basic Forms
```jsx
function SimpleForm() {
  const [formData, setFormData] = useState({
    username: '',
    email: ''
  });

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  return (
    <form>
      <input
        name="username"
        value={formData.username}
        onChange={handleChange}
      />
      <input
        name="email"
        value={formData.email}
        onChange={handleChange}
      />
    </form>
  );
}
```

Key concepts:
- Form controls
- Controlled components
- Form submission
- Basic validation

### 7. Component Lifecycle with useEffect
```jsx
function DataFetcher() {
  const [data, setData] = useState(null);

  useEffect(() => {
    fetch('https://api.example.com/data')
      .then(response => response.json())
      .then(data => setData(data));
  }, []);

  return <div>{/* Render data */}</div>;
}
```

Key concepts:
- Effect dependencies
- Cleanup functions
- API calls
- Side effects

### 8. Props and PropTypes
```jsx
import PropTypes from 'prop-types';

function User({ name, age, email }) {
  return (
    <div>
      <h2>{name}</h2>
      <p>Age: {age}</p>
      <p>Email: {email}</p>
    </div>
  );
}

User.propTypes = {
  name: PropTypes.string.isRequired,
  age: PropTypes.number,
  email: PropTypes.string
};
```

Key concepts:
- Props validation
- Default props
- Children prop
- Props drilling

### 9. Basic Styling
```jsx
// CSS Module
import styles from './Button.module.css';

function Button() {
  return (
    <button className={styles.button}>
      Click me
    </button>
  );
}

// Inline Styles
function Card() {
  return (
    <div style={{
      padding: '20px',
      border: '1px solid #ccc'
    }}>
      Content
    </div>
  );
}
```

Key concepts:
- CSS Modules
- Inline styles
- className usage
- Style organization

### 10. Error Handling
```jsx
class ErrorBoundary extends React.Component {
  state = { hasError: false };

  static getDerivedStateFromError(error) {
    return { hasError: true };
  }

  render() {
    if (this.state.hasError) {
      return <h1>Something went wrong.</h1>;
    }
    return this.props.children;
  }
}
```

Key concepts:
- Try-catch blocks
- Error boundaries
- Error messages
- Fallback UI

## Intermediate Level Features

### 1. Advanced Hooks
```jsx
function useCustomHook(initialValue) {
  const [value, setValue] = useState(initialValue);
  const [history, setHistory] = useState([initialValue]);

  const updateValue = (newValue) => {
    setValue(newValue);
    setHistory([...history, newValue]);
  };

  return [value, updateValue, history];
}
```

Key concepts:
- Custom hooks
- useCallback
- useMemo
- useRef

### 2. Context API
```jsx
const ThemeContext = React.createContext('light');

function ThemeProvider({ children }) {
  const [theme, setTheme] = useState('light');

  return (
    <ThemeContext.Provider value={{ theme, setTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}
```

Key concepts:
- Context creation
- Providers
- Consumers
- Context optimization

### 3. Advanced State Management
```jsx
function reducer(state, action) {
  switch (action.type) {
    case 'increment':
      return { count: state.count + 1 };
    case 'decrement':
      return { count: state.count - 1 };
    default:
      throw new Error();
  }
}

function Counter() {
  const [state, dispatch] = useReducer(reducer, { count: 0 });
  return (
    <>
      Count: {state.count}
      <button onClick={() => dispatch({ type: 'increment' })}>+</button>
    </>
  );
}
```

Key concepts:
- useReducer
- Action types
- State immutability
- Complex state logic

### 4. React Router
```jsx
import { BrowserRouter, Route, Switch } from 'react-router-dom';

function App() {
  return (
    <BrowserRouter>
      <Switch>
        <Route exact path="/" component={Home} />
        <Route path="/about" component={About} />
        <Route path="/users/:id" component={UserProfile} />
      </Switch>
    </BrowserRouter>
  );
}
```

Key concepts:
- Route configuration
- Navigation
- Route parameters
- Protected routes

### 5. Form Management
```jsx
import { useForm } from 'react-hook-form';

function AdvancedForm() {
  const { register, handleSubmit, errors } = useForm();

  const onSubmit = (data) => console.log(data);

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input {...register('username', { required: true })} />
      {errors.username && <span>Required field</span>}
    </form>
  );
}
```

Key concepts:
- Form libraries
- Validation
- Error handling
- Form state

### 6. API Integration
```jsx
function useAPI(url) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await fetch(url);
        const json = await response.json();
        setData(json);
        setLoading(false);
      } catch (error) {
        setError(error);
        setLoading(false);
      }
    };

    fetchData();
  }, [url]);

  return { data, loading, error };
}
```

Key concepts:
- REST APIs
- GraphQL
- Error handling
- Loading states

### 7. Performance Optimization
```jsx
const MemoizedComponent = React.memo(function MyComponent(props) {
  return (
    <div>
      {/* Expensive rendering */}
    </div>
  );
});

function Parent() {
  const memoizedCallback = useCallback(
    () => {
      // Callback logic
    },
    []
  );

  return <MemoizedComponent onCallback={memoizedCallback} />;
}
```

Key concepts:
- React.memo
- useCallback
- useMemo
- Code splitting

### 8. Testing
```jsx
import { render, fireEvent } from '@testing-library/react';

test('button click increments counter', () => {
  const { getByText } = render(<Counter />);
  const button = getByText('Increment');
  
  fireEvent.click(button);
  
  expect(getByText('Count: 1')).toBeInTheDocument();
});
```

Key concepts:
- Unit testing
- Integration testing
- Test utilities
- Mocking

### 9. Styled Components
```jsx
import styled from 'styled-components';

const Button = styled.button`
  background: ${props => props.primary ? 'blue' : 'white'};
  color: ${props => props.primary ? 'white' : 'blue'};
  padding: 10px 20px;
  border: 2px solid blue;
  border-radius: 4px;
`;

function Component() {
  return <Button primary>Click me</Button>;
}
```

Key concepts:
- CSS-in-JS
- Theme system
- Dynamic styles
- Global styles

### 10. TypeScript Integration
```typescript
interface Props {
  name: string;
  age: number;
  email?: string;
}

function User({ name, age, email }: Props) {
  return (
    <div>
      <h2>{name}</h2>
      <p>Age: {age}</p>
      {email && <p>Email: {email}</p>}
    </div>
  );
}
```

Key concepts:
- Type definitions
- Interfaces
- Generics
- Type inference

## Advanced Level Features

### 1. Advanced Patterns
```jsx
// Render Props Pattern
class MouseTracker extends React.Component {
  state = { x: 0, y: 0 };

  handleMouseMove = (event) => {
    this.setState({
      x: event.clientX,
      y: event.clientY
    });
  };

  render() {
    return (
      <div onMouseMove={this.handleMouseMove}>
        {this.props.render(this.state)}
      </div>
    );
  }
}

// Usage
<MouseTracker
  render={({ x, y }) => (
    <h1>Mouse position: {x}, {y}</h1>
  )}
/>
```

Key concepts:
- Render props
- Higher-Order Components
- Compound components
- Custom hooks patterns

### 2. Server-Side Rendering
```jsx
// Next.js example
export async function getServerSideProps() {
  const res = await fetch('https://api.example.com/data');
  const data = await res.json();

  return {
    props: {
      data
    }
  };
}

function Page({ data }) {
  return <div>{/* Render data */}</div>;
}
```

Key concepts:
- Next.js
- Data fetching
- Static generation
- Hydration

### 3. State Management Libraries
```jsx
// Redux example
const counterSlice = createSlice({
  name: 'counter',
  initialState: { value: 0 },
  reducers: {
    increment: state => {
      state.value += 1;
    }
  }
});

function Counter() {
  const count = useSelector(state => state.counter.value);
  const dispatch = useDispatch();

  return (
    <button onClick={() => dispatch(increment())}>
      Count: {count}
    </button>
  );
}
```

Key concepts:
- Redux
- MobX
- Recoil
- Zustand

### 4. Performance Profiling
```jsx
const Profiler = ({ children }) => {
  const handleProfiler = (
    id,
    phase,
    actualDuration,
    baseDuration,
    startTime,
    commitTime,
    interactions
  ) => {
    console.log({
      id,
      phase,
      actualDuration,
      baseDuration
    });
  };

  return (
    <React.Profiler id="App" onRender={handleProfiler}>
      {children}
    </React.Profiler>
  );
};
```

Key concepts:
- React DevTools
- Performance metrics
- Optimization strategies
- Bundle analysis

### 5. Animation
```jsx
import { motion } from 'framer-motion';

function AnimatedComponent() {
  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.5 }}
    >
      Animated content
    </motion.div>
  );
}
```

Key concepts:
- CSS transitions
- React Spring
- Framer Motion
- GSAP

### 6. Accessibility
```jsx
function AccessibleForm() {
  return (
    <form aria-labelledby="form-title">
      <h2 id="form-title">Contact Form</h2>
      <label htmlFor="name">Name:</label>
      <input
        id="name"
        aria-required="true"
        aria-invalid={errors.name ? "true" : "false"}
      />
    </form>
  );
}
```

Key concepts:
- ARIA attributes
- Keyboard navigation
- Screen readers
- Focus management

### 7. Internationalization
```jsx
import { useTranslation } from 'react-i18next';

function Welcome() {
  const { t } = useTranslation();

  return (
    <div>
      <h1>{t('welcome.title')}</h1>
      <p>{t('welcome.description')}</p>
    </div>
  );
}
```

Key concepts:
- i18next
- Locale management
- RTL support
- Date/number formatting

### 8. Code Splitting
```jsx
const LazyComponent = React.lazy(() => import('./LazyComponent'));

function App() {
  return (
    <Suspense fallback={<Loading />}>
      <LazyComponent />
    </Suspense>
  );
}
```

Key concepts:
- Dynamic imports
- Route-based splitting
- Component-based splitting
- Prefetching

### 9. Security
```jsx
function SecureComponent() {
  const sanitizeHTML = (html) => {
    // Sanitization logic
  };

  return (
    <div
      dangerouslySetInnerHTML={{
        __html: sanitize
