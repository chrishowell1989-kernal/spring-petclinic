import { useEffect, useState } from 'react'
import { apiClient } from '../api/client'
import ErrorPage from './ErrorPage'

/** Demo page for /oups — triggers the backend's diagnostic crash endpoint
 *  (GET /api/oups) to show what an unhandled 500 looks like, mirroring the
 *  original Thymeleaf CrashController demo. */
export default function CrashPage() {
  const [message, setMessage] = useState<string>()

  useEffect(() => {
    apiClient.get('/oups').catch(() => {
      setMessage('Expected: controller used to showcase what happens when an exception is thrown')
    })
  }, [])

  return <ErrorPage status={500} message={message} />
}
