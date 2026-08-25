import { createTheme } from '@mui/material/styles'

// Brand palette carried over from the former src/main/scss/petclinic.scss
const springGreen = '#6db33f'
const springDarkGreen = '#5fa134'
const springBrown = '#34302d'

export const theme = createTheme({
  palette: {
    primary: {
      main: springGreen,
      dark: springDarkGreen,
    },
    text: {
      primary: springBrown,
    },
  },
  shape: {
    borderRadius: 4,
  },
})
