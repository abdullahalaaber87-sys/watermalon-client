import { Apple, Box, Pickaxe, Shield, Sword } from 'lucide-react';
import { useRef, useState, type CSSProperties, type KeyboardEvent, type PointerEvent, type ReactNode } from 'react';
import type { HudElementId, HudLayout, HudOptions, HudPoint, Track } from '../clientData';
import { BrandMark, SpotifyWidget } from './ClientPieces';

export type HudStats = { fps: number; ping: number; x: number; y: number; z: number; facing: string };
export type HudKeys = { w: boolean; a: boolean; s: boolean; d: boolean; space: boolean; lmb: boolean; rmb: boolean; lcps: number; rcps: number };
export type CrosshairStyle = { enabled: boolean; style: string; size: number };

type GameHudProps = {
  layout: HudLayout;
  setPoint: (id: HudElementId, point: HudPoint) => void;
  editing: boolean;
  visible: Record<HudElementId, boolean>;
  scale: number;
  options: HudOptions;
  arrayNames: string[];
  track: Track;
  trackPosition: number;
  playing: boolean;
  stats: HudStats;
  keys: HudKeys;
  crosshair: CrosshairStyle;
  hotbarSlot: number;
};

const clamp = (value: number) => Math.max(0, Math.min(1, value));
const SNAP = 0.012;

function snap(value: number) {
  if (Math.abs(value - 0.5) < SNAP) return { value: 0.5, guide: true };
  if (value < SNAP) return { value: 0, guide: false };
  if (value > 1 - SNAP) return { value: 1, guide: false };
  return { value, guide: false };
}

export function GameHud(props: GameHudProps) {
  const { layout, setPoint, editing, visible, scale, options, arrayNames, track, trackPosition, playing, stats, keys, crosshair, hotbarSlot } = props;
  const stageRef = useRef<HTMLDivElement>(null);
  const drag = useRef<{ id: HudElementId; grabX: number; grabY: number } | null>(null);
  const [selected, setSelected] = useState<HudElementId | null>(null);
  const [guides, setGuides] = useState({ x: false, y: false });

  function startDrag(id: HudElementId, event: PointerEvent<HTMLDivElement>) {
    if (!editing || event.button !== 0) return;
    const rect = event.currentTarget.getBoundingClientRect();
    drag.current = { id, grabX: event.clientX - rect.left, grabY: event.clientY - rect.top };
    event.currentTarget.setPointerCapture(event.pointerId);
    event.currentTarget.focus();
    setSelected(id);
  }

  function moveDrag(event: PointerEvent<HTMLDivElement>) {
    if (!drag.current || !stageRef.current) return;
    const stage = stageRef.current.getBoundingClientRect();
    const item = event.currentTarget.getBoundingClientRect();
    const sx = snap(clamp((event.clientX - stage.left - drag.current.grabX) / Math.max(1, stage.width - item.width)));
    const sy = snap(clamp((event.clientY - stage.top - drag.current.grabY) / Math.max(1, stage.height - item.height)));
    setGuides({ x: sx.guide, y: sy.guide });
    setPoint(drag.current.id, { x: sx.value, y: sy.value });
  }

  function endDrag() {
    drag.current = null;
    setGuides({ x: false, y: false });
  }

  function nudge(id: HudElementId, event: KeyboardEvent<HTMLDivElement>) {
    if (!editing || !['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(event.key)) return;
    event.preventDefault();
    event.stopPropagation();
    const step = event.shiftKey ? 0.04 : 0.006;
    const point = layout[id];
    setPoint(id, {
      x: clamp(point.x + (event.key === 'ArrowRight' ? step : event.key === 'ArrowLeft' ? -step : 0)),
      y: clamp(point.y + (event.key === 'ArrowDown' ? step : event.key === 'ArrowUp' ? -step : 0)),
    });
  }

  function element(id: HudElementId, label: string, children: ReactNode) {
    if (!visible[id]) return null;
    const point = layout[id];
    const style = {
      left: `${point.x * 100}%`,
      top: `${point.y * 100}%`,
      transform: `translate(-${point.x * 100}%, -${point.y * 100}%)`,
    } as CSSProperties;
    return (
      <div
        key={id}
        className={`hud-element hud-el-${id}${editing ? ' is-editable' : ''}${selected === id && editing ? ' is-selected' : ''}`}
        style={style}
        data-hud={id}
        onPointerDown={(event) => startDrag(id, event)}
        onPointerMove={moveDrag}
        onPointerUp={endDrag}
        onPointerCancel={endDrag}
        onKeyDown={(event) => nudge(id, event)}
        onFocus={() => editing && setSelected(id)}
        role={editing ? 'button' : undefined}
        tabIndex={editing ? 0 : undefined}
        aria-label={editing ? `Move ${label}. Drag or use arrow keys.` : undefined}
      >
        <div className="hud-zoom" style={{ zoom: scale } as CSSProperties}>{children}</div>
        {editing && <span className="hud-element-tag">{label}</span>}
      </div>
    );
  }

  const arrayRight = layout.arraylist.x > 0.5;

  return (
    <div className={`game-hud${editing ? ' is-editing' : ''}`} ref={stageRef} onPointerDown={(event) => { if (event.target === event.currentTarget) setSelected(null); }}>
      {editing && <div className="hud-grid" aria-hidden="true" />}
      {guides.x && <div className="hud-guide hud-guide-x" aria-hidden="true" />}
      {guides.y && <div className="hud-guide hud-guide-y" aria-hidden="true" />}

      <Crosshair {...crosshair} />

      {element('watermark', 'Watermark', (
        <div className="hud-card hud-watermark-card">
          <BrandMark small />
          <div><strong>WATER<span>MALON</span></strong><small>1.21.11</small></div>
          <em>{stats.fps} fps</em>
        </div>
      ))}

      {element('arraylist', 'ArrayList', (
        <div className={`hud-arraylist${arrayRight ? ' is-right' : ''}`}>
          {arrayNames.length ? arrayNames.map((name, index) => (
            <span key={name} style={{ '--i': index } as CSSProperties}>{name}<i /></span>
          )) : <span className="is-empty">No modules enabled<i /></span>}
        </div>
      ))}

      {element('spotify', 'Spotify', (
        <div className="hud-spotify"><SpotifyWidget track={track} position={trackPosition} options={options} playing={playing} /></div>
      ))}

      {element('info', 'Info', (
        <div className="hud-card hud-info-card">
          <span><b>{stats.fps}</b> FPS</span>
          <span><b>{stats.ping}</b> MS</span>
          <span>XYZ <b>{Math.round(stats.x)} {Math.round(stats.y)} {Math.round(stats.z)}</b></span>
          <span><b>{stats.facing}</b></span>
        </div>
      ))}

      {element('keystrokes', 'Keystrokes', (
        <div className="hud-keystrokes">
          <div className="ks-row"><span className={`ks-key${keys.w ? ' down' : ''}`}>W</span></div>
          <div className="ks-row">
            <span className={`ks-key${keys.a ? ' down' : ''}`}>A</span>
            <span className={`ks-key${keys.s ? ' down' : ''}`}>S</span>
            <span className={`ks-key${keys.d ? ' down' : ''}`}>D</span>
          </div>
          <div className="ks-row">
            <span className={`ks-key ks-mouse${keys.lmb ? ' down' : ''}`}>LMB<small>{keys.lcps} CPS</small></span>
            <span className={`ks-key ks-mouse${keys.rmb ? ' down' : ''}`}>RMB<small>{keys.rcps} CPS</small></span>
          </div>
          <div className="ks-row"><span className={`ks-key ks-space${keys.space ? ' down' : ''}`}><i /></span></div>
        </div>
      ))}

      <Hotbar slot={hotbarSlot} />
    </div>
  );
}

function Crosshair({ enabled, style, size }: CrosshairStyle) {
  if (!enabled) return <div className="hud-crosshair vanilla" aria-hidden="true" />;
  return <div className={`hud-crosshair custom ${style.toLowerCase()}`} style={{ '--ch': `${size * 1.6}px` } as CSSProperties} aria-hidden="true" />;
}

const hotbarItems = [Sword, Pickaxe, Box, Box, Apple, Shield, null, null, null];

function Hotbar({ slot }: { slot: number }) {
  return (
    <div className="hud-hotbar" aria-hidden="true">
      <div className="hud-vitals">
        <div className="hud-hearts">{Array.from({ length: 10 }, (_, i) => <i key={i} className={i < 9 ? 'full' : 'half'} />)}</div>
        <div className="hud-food">{Array.from({ length: 10 }, (_, i) => <i key={i} />)}</div>
      </div>
      <div className="hud-xp"><span style={{ width: '62%' }} /><b>27</b></div>
      <div className="hud-slots">
        {hotbarItems.map((Icon, index) => (
          <span key={index} className={index === slot ? 'active' : ''}>
            {Icon && <Icon size={16} strokeWidth={1.8} />}
            {index === 2 && <small>64</small>}
            {index === 3 && <small>32</small>}
            {index === 4 && <small>12</small>}
          </span>
        ))}
      </div>
    </div>
  );
}
