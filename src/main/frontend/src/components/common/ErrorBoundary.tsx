import { Component } from 'react'
import type { ErrorInfo, ReactNode } from 'react'
import ErrorPage from '../../pages/ErrorPage'

interface Props {
  children: ReactNode
}

interface State {
  hasError: boolean
}

export default class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false }

  static getDerivedStateFromError(): State {
    return { hasError: true }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('Unhandled UI error', error, info)
  }

  render() {
    if (this.state.hasError) {
      return <ErrorPage status={500} />
    }
    return this.props.children
  }
}
