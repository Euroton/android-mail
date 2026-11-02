# Keep only workspace members that exist in this public checkout.
import os,re,sys
p=os.path.expanduser('~/pc/Cargo.toml'); s=open(p).read()
if not os.path.exists(p+'.orig'): open(p+'.orig','w').write(s)
s=open(p+'.orig').read()
m=re.search(r'members = \[(.*?)\n\]',s,re.S)
keep=[];drop=[]
for line in m.group(1).split('\n'):
    q=re.search(r'"([^"]+)"',line)
    if not q: keep.append(line); continue
    (keep if os.path.exists(os.path.expanduser('~/pc/'+q.group(1))) else drop).append(line)
s=s[:m.start(1)]+'\n'.join(keep)+s[m.end(1):]
open(p,'w').write(s); print('dropped',len(drop),'kept',sum(1 for l in keep if '"' in l))
for l in drop[:6]: print(' ',l.strip())
