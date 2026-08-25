import { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import Snackbar from '@mui/material/Snackbar'
import Alert from '@mui/material/Alert'

interface FlashState {
  message?: string
  severity?: 'success' | 'error'
}

/** Reads a one-shot flash message from router navigation state, replacing
 *  the old Thymeleaf flash-attribute + auto-dismissing inline <script>. */
export default function FlashSnackbar() {
  const location = useLocation()
  const state = (location.state ?? {}) as FlashState
  const [open, setOpen] = useState(Boolean(state.message))

  useEffect(() => {
    setOpen(Boolean(state.message))
  }, [state.message])

  // Local state only: no navigate() here. The Snackbar's autoHideDuration timer can
  // fire well after the user has already navigated elsewhere, and re-navigating from a
  // stale `location` closure at that point would silently bounce them back — this bit
  // us as a real bug where clicking "Edit Owner"/"Add New Pet" right after a flash
  // message appeared would revert the URL a few seconds later.
  const handleClose = () => setOpen(false)

  if (!state.message) {
    return null
  }

  return (
    <Snackbar open={open} autoHideDuration={3000} onClose={handleClose}>
      <Alert onClose={handleClose} severity={state.severity ?? 'success'} variant="filled">
        {state.message}
      </Alert>
    </Snackbar>
  )
}
