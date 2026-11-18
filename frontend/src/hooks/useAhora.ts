import { useEffect, useState } from 'react'

export function useAhora() {
  const [ahora, setAhora] = useState(Date.now)
  useEffect(() => {
    const intervalo = window.setInterval(() => setAhora(Date.now()), 15000)
    return () => window.clearInterval(intervalo)
  }, [])
  return ahora
}
