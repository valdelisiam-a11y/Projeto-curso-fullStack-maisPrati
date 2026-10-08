import { useState } from 'react'
import Button from './components/Button/Button.jsx'
import Input from './components/Input/Input.jsx'
import Message from './components/Message/Message.jsx'
import './App.css'

function App() {
  const [email, setEmail] = useState('')
  const [message, setMessage] = useState(null)

  function handleChange(event) {
    setEmail(event.target.value)
    setMessage(null)
  }

  function handleSubmit(event) {
    event.preventDefault()

    if (!email.trim()) {
      setMessage({ type: 'error', text: 'Confira seus dados.' })
      return
    }

    setMessage({
      type: 'success',
      text: 'E-mail validado. Esta tela ainda não está conectada à autenticação.',
    })
  }

  return (
    <main className="demo-page">
      <section className="demo-card" aria-labelledby="demo-title">
        <span className="demo-eyebrow">DOCFLOW</span>
        <h1 id="demo-title">Acesse o DocFlow</h1>
        <p className="demo-description">
          Um exemplo de uso dos componentes reutilizáveis do projeto.
        </p>

        <form
          className="demo-form"
          onSubmit={handleSubmit}
          onInvalid={(event) => {
            event.preventDefault()
            setMessage({ type: 'error', text: 'Confira seus dados.' })
          }}
        >
          <Input
            label="E-mail"
            type="email"
            name="email"
            value={email}
            onChange={handleChange}
            placeholder="voce@exemplo.com"
            required
          />

          {message && <Message type={message.type}>{message.text}</Message>}

          <Button type="submit">Entrar</Button>
        </form>
      </section>
    </main>
  )
}

export default App