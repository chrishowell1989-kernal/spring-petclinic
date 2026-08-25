import TextField from '@mui/material/TextField'
import type { TextFieldProps } from '@mui/material/TextField'

type FormTextFieldProps = Omit<TextFieldProps, 'variant'> & {
  errorMessage?: string
}

export default function FormTextField({ errorMessage, ...props }: FormTextFieldProps) {
  return (
    <TextField
      fullWidth
      variant="outlined"
      margin="normal"
      error={Boolean(errorMessage)}
      helperText={errorMessage}
      {...props}
    />
  )
}
