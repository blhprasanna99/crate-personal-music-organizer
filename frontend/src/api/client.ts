import type { Crate, Filter, ImportSummary, SmartCrate, Track } from './types'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(path, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...(init?.headers || {}) },
  })
  if (!res.ok) {
    const body = await res.text()
    throw new Error(`${res.status} ${res.statusText}: ${body}`)
  }
  if (res.status === 204) return undefined as T
  return res.json()
}

export const api = {
  // tracks
  listTracks: () => request<Track[]>('/api/tracks'),
  searchTracks: (criteria: Filter[]) =>
    request<Track[]>('/api/tracks/search', {
      method: 'POST',
      body: JSON.stringify({ criteria }),
    }),
  importFolder: (folderPath: string) =>
    request<ImportSummary>('/api/tracks/import', {
      method: 'POST',
      body: JSON.stringify({ folderPath }),
    }),

  // crates
  listCrates: () => request<Crate[]>('/api/crates'),
  createCrate: (name: string, description?: string) =>
    request<Crate>('/api/crates', {
      method: 'POST',
      body: JSON.stringify({ name, description }),
    }),
  deleteCrate: (id: number) =>
    request<void>(`/api/crates/${id}`, { method: 'DELETE' }),
  crateTracks: (id: number, recursive = false) =>
    request<Track[]>(`/api/crates/${id}/tracks${recursive ? '?recursive=true' : ''}`),

  // smart crates
  listSmartCrates: () => request<SmartCrate[]>('/api/smart-crates'),
  smartCrateTracks: (id: number) =>
    request<Track[]>(`/api/smart-crates/${id}/tracks`),
}
