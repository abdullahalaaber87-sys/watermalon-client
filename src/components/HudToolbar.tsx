import { Check, RotateCcw } from 'lucide-react';
import type { CSSProperties } from 'react';
import { hudElements, type HudElementId } from '../clientData';
import { BrandMark } from './ClientPieces';

type HudToolbarProps = {
  shown: Record<HudElementId, boolean>;
  toggle: (id: HudElementId) => void;
  scale: number;
  setScale: (scale: number) => void;
  hudEnabled: boolean;
  enableHud: () => void;
  reset: () => void;
  done: () => void;
};

export function HudToolbar({ shown, toggle, scale, setScale, hudEnabled, enableHud, reset, done }: HudToolbarProps) {
  return (
    <div className="hud-toolbar" role="toolbar" aria-label="HUD layout editor">
      <div className="hud-toolbar-title"><BrandMark small /><div><strong>HUD layout</strong><small>Drag anything. Arrows nudge.</small></div></div>
      <div className="hud-toolbar-chips">
        {hudElements.map(({ id, label }) => (
          <button key={id} className={shown[id] ? 'on' : ''} aria-pressed={shown[id]} onClick={() => toggle(id)}>
            <i>{shown[id] && <Check size={9} strokeWidth={3} />}</i>{label}
          </button>
        ))}
      </div>
      <label className="hud-toolbar-scale">
        <span>Scale <b>{scale}%</b></span>
        <input type="range" min="70" max="150" step="5" value={scale} aria-label="HUD scale" onChange={(event) => setScale(Number(event.target.value))} style={{ '--range-progress': `${((scale - 70) / 80) * 100}%` } as CSSProperties} />
      </label>
      <div className="hud-toolbar-actions">
        <button className="ghost" onClick={reset}><RotateCcw size={12} /> Reset</button>
        <button className="primary" onClick={done}>Done</button>
      </div>
      {!hudEnabled && <div className="hud-toolbar-notice">HUD module is off. <button onClick={enableHud}>Turn it on</button></div>}
    </div>
  );
}
