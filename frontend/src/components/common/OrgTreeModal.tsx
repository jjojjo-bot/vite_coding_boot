import { useState } from 'react';
import type { User } from '../../types';

interface Props {
  members: User[];
  open: boolean;
  onClose: () => void;
  onSelect: (userId: number, userName: string) => void;
}

interface TreeNode {
  [dept: string]: { [team: string]: User[] };
}

export default function OrgTreeModal({ members, open, onClose, onSelect }: Props) {
  const [collapsed, setCollapsed] = useState<Set<string>>(new Set());

  if (!open) return null;

  const tree: Record<string, TreeNode> = {};
  members.forEach((m) => {
    const div = m.teamFullName?.split(' > ')[0] || '소속없음';
    const dept = m.teamFullName?.split(' > ')[1] || '';
    const team = m.teamFullName?.split(' > ')[2] || '';
    if (!tree[div]) tree[div] = {};
    if (!tree[div][dept]) tree[div][dept] = {};
    if (!tree[div][dept][team]) tree[div][dept][team] = [];
    tree[div][dept][team].push(m);
  });

  const toggle = (key: string) => {
    setCollapsed((prev) => {
      const next = new Set(prev);
      if (next.has(key)) next.delete(key);
      else next.add(key);
      return next;
    });
  };

  return (
    <div className="org-modal-overlay active" onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div className="org-modal">
        <div className="org-modal-header">
          <span>조직도 - 담당자 선택</span>
          <button className="org-modal-close" onClick={onClose}>&times;</button>
        </div>
        <div className="org-modal-body">
          <div className="org-tree">
            <ul>
              {Object.entries(tree).map(([div, depts]) => (
                <li key={div}>
                  <div className={`org-tree-toggle ${collapsed.has(div) ? 'collapsed' : ''}`} onClick={() => toggle(div)}>{div}</div>
                  {!collapsed.has(div) && (
                    <ul>
                      {Object.entries(depts).map(([dept, teams]) => (
                        <li key={dept}>
                          <div className={`org-tree-toggle ${collapsed.has(div + dept) ? 'collapsed' : ''}`} onClick={() => toggle(div + dept)}>{dept}</div>
                          {!collapsed.has(div + dept) && (
                            <ul>
                              {Object.entries(teams).map(([team, users]) => (
                                <li key={team}>
                                  <div className={`org-tree-toggle ${collapsed.has(div + dept + team) ? 'collapsed' : ''}`} onClick={() => toggle(div + dept + team)}>{team}</div>
                                  {!collapsed.has(div + dept + team) && (
                                    <ul>
                                      {users.map((u) => (
                                        <li key={u.id}>
                                          <div className="org-tree-user" onClick={() => { onSelect(u.id, u.name); onClose(); }}>
                                            {u.name} ({u.username})
                                          </div>
                                        </li>
                                      ))}
                                    </ul>
                                  )}
                                </li>
                              ))}
                            </ul>
                          )}
                        </li>
                      ))}
                    </ul>
                  )}
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}
