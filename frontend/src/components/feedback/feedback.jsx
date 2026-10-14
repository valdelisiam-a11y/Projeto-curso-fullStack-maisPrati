


import './Feedback.css';


export const Feedback = ({ type, message }) => {
  
  if (!message) return null;

  
  const icon = type === 'success' ? '✓' : '✕';

  
  return (
    <div className={`feedback-card feedback-${type}`} role="alert">
      
      <span className="feedback-icon">{icon}</span>

      
      <p className="feedback-text">{message}</p>
    </div>
  );
};


export default Feedback;