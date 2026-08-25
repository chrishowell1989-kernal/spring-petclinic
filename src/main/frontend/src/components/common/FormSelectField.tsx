import FormControl from '@mui/material/FormControl'
import InputLabel from '@mui/material/InputLabel'
import Select from '@mui/material/Select'
import MenuItem from '@mui/material/MenuItem'
import FormHelperText from '@mui/material/FormHelperText'

export interface SelectOption {
  value: string
  label: string
}

interface FormSelectFieldProps {
  label: string
  name: string
  value: string
  options: SelectOption[]
  onChange: (value: string) => void
  errorMessage?: string
}

export default function FormSelectField({
  label,
  name,
  value,
  options,
  onChange,
  errorMessage,
}: FormSelectFieldProps) {
  return (
    <FormControl fullWidth margin="normal" error={Boolean(errorMessage)}>
      <InputLabel id={`${name}-label`}>{label}</InputLabel>
      <Select
        labelId={`${name}-label`}
        label={label}
        name={name}
        value={value}
        onChange={(e) => onChange(e.target.value)}
      >
        {options.map((option) => (
          <MenuItem key={option.value} value={option.value}>
            {option.label}
          </MenuItem>
        ))}
      </Select>
      {errorMessage && <FormHelperText>{errorMessage}</FormHelperText>}
    </FormControl>
  )
}
