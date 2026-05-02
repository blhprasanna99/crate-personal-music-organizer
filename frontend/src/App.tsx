import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from './api/client'
import type { Track } from './api/types'

type Selection =
  | { kind: 'all' }
  | { kind: 'crate'; id: number; name: string }
  | { kind: 'smart'; id: number; name: string }

function fmtDuration(ms: number | null): string {
  if (!ms) return ''
  const total = Math.round(ms / 1000)
  const m = Math.floor(total / 60)
  const s = total % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}

export default function App() {
  const [selection, setSelection] = useState<Selection>({ kind: 'all' })
  const [search, setSearch] = useState('')
  const [showNewCrate, setShowNewCrate] = useState(false)
  const [showImport, setShowImport] = useState(false)

  const crates = useQuery({ queryKey: ['crates'], queryFn: api.listCrates })
  const smartCrates = useQuery({ queryKey: ['smart-crates'], queryFn: api.listSmartCrates })

  const tracks = useQuery({
    queryKey: ['tracks', selection, search],
    queryFn: async (): Promise<Track[]> => {
      if (search.trim()) {
        return api.searchTracks([{ op: 'titleContains', value: search.trim() }])
      }
      switch (selection.kind) {
        case 'all':   return api.listTracks()
        case 'crate': return api.crateTracks(selection.id)
        case 'smart': return api.smartCrateTracks(selection.id)
      }
    },
  })

  const isActive = (s: Selection) =>
    s.kind === selection.kind &&
    ('id' in s && 'id' in selection ? s.id === (selection as any).id : true)

  return (
    <div className="app">
      <aside className="sidebar">
        <h2>Library</h2>
        <div
          className={'crate-link ' + (isActive({ kind: 'all' }) ? 'active' : '')}
          onClick={() => setSelection({ kind: 'all' })}
        >
          📚 All tracks
        </div>

        <h2>Crates</h2>
        {crates.data?.map(c => (
          <div
            key={c.id}
            className={'crate-link ' + (isActive({ kind: 'crate', id: c.id, name: c.name }) ? 'active' : '')}
            onClick={() => setSelection({ kind: 'crate', id: c.id, name: c.name })}
          >
            📂 {c.name}
          </div>
        ))}
        <button style={{ marginTop: 6, width: '100%' }} onClick={() => setShowNewCrate(true)}>
          + new crate
        </button>

        <h2>Smart crates</h2>
        {smartCrates.data?.length === 0 && <div className="muted" style={{ fontSize: 13 }}>none yet</div>}
        {smartCrates.data?.map(s => (
          <div
            key={s.id}
            className={'crate-link ' + (isActive({ kind: 'smart', id: s.id, name: s.name }) ? 'active' : '')}
            onClick={() => setSelection({ kind: 'smart', id: s.id, name: s.name })}
          >
            🔍 {s.name}
          </div>
        ))}
      </aside>

      <main className="main">
        <div className="toolbar">
          <input
            type="text"
            placeholder="Search by title…"
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
          <button onClick={() => setShowImport(true)}>Import folder</button>
        </div>

        <h2 style={{ marginTop: 0 }}>
          {selection.kind === 'all' ? 'All tracks' :
           selection.kind === 'crate' ? `📂 ${selection.name}` :
           `🔍 ${selection.name}`}
          {' '}
          <span className="muted" style={{ fontSize: 14 }}>
            {tracks.data ? `(${tracks.data.length})` : ''}
          </span>
        </h2>

        {tracks.isLoading && <div className="muted">loading…</div>}
        {tracks.error && <div className="error">{(tracks.error as Error).message}</div>}
        {tracks.data && tracks.data.length === 0 && (
          <div className="muted">no tracks here</div>
        )}
        {tracks.data && tracks.data.length > 0 && (
          <table className="track-table">
            <thead>
              <tr>
                <th>Title</th><th>Artist</th><th>Album</th><th>Duration</th>
              </tr>
            </thead>
            <tbody>
              {tracks.data.map(t => (
                <tr key={t.id}>
                  <td>{t.title || <span className="muted">{t.filePath.split('/').pop()}</span>}</td>
                  <td>{t.artist || ''}</td>
                  <td>{t.album || ''}</td>
                  <td className="muted">{fmtDuration(t.durationMs)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </main>

      {showNewCrate && <NewCrateDialog onClose={() => setShowNewCrate(false)} />}
      {showImport && <ImportDialog onClose={() => setShowImport(false)} />}
    </div>
  )
}

function NewCrateDialog({ onClose }: { onClose: () => void }) {
  const qc = useQueryClient()
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const create = useMutation({
    mutationFn: () => api.createCrate(name, description || undefined),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['crates'] }); onClose() },
  })

  return (
    <div className="dialog-backdrop" onClick={onClose}>
      <div className="dialog" onClick={e => e.stopPropagation()}>
        <h3>New crate</h3>
        <input placeholder="name" value={name} onChange={e => setName(e.target.value)} autoFocus />
        <input placeholder="description (optional)" value={description} onChange={e => setDescription(e.target.value)} />
        {create.error && <div className="error">{(create.error as Error).message}</div>}
        <div className="row">
          <button onClick={onClose}>Cancel</button>
          <button disabled={!name || create.isPending} onClick={() => create.mutate()}>
            {create.isPending ? 'Creating…' : 'Create'}
          </button>
        </div>
      </div>
    </div>
  )
}

function ImportDialog({ onClose }: { onClose: () => void }) {
  const qc = useQueryClient()
  const [folderPath, setFolderPath] = useState('')
  const importMut = useMutation({
    mutationFn: () => api.importFolder(folderPath),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['tracks'] }),
  })

  return (
    <div className="dialog-backdrop" onClick={onClose}>
      <div className="dialog" onClick={e => e.stopPropagation()}>
        <h3>Import folder</h3>
        <input placeholder="/Users/you/Music" value={folderPath} onChange={e => setFolderPath(e.target.value)} autoFocus />
        {importMut.error && <div className="error">{(importMut.error as Error).message}</div>}
        {importMut.data && (
          <div className="muted" style={{ fontSize: 13 }}>
            scanned {importMut.data.scanned}, imported {importMut.data.imported},
            skipped {importMut.data.skipped}, failed {importMut.data.failed}
          </div>
        )}
        <div className="row">
          <button onClick={onClose}>Close</button>
          <button disabled={!folderPath || importMut.isPending} onClick={() => importMut.mutate()}>
            {importMut.isPending ? 'Importing…' : 'Import'}
          </button>
        </div>
      </div>
    </div>
  )
}
