q0 _ (m0, _, L)
m0 0 (r0, _, R)
m0 1 (r1, _, R)
m0 _ (q1, _, R)
r0 _ (s0, 0, S)
r1 _ (s0, 1, S)
s0 * (s0, *, L)
s0 _ (m0, _, L)
q1 _ (h0, _, R)
h0 0 (p0, _, L)
h0 1 (p1, _, L)
h0 _ (mb, _, S)
p0 _ (d0, 0, R)
p1 _ (d1, 1, R)
d0 _ (d0', _, R)
d0' * (d0', *, R)
d0' _ (d0'', _, R)
d0'' * (d0'', *, R)
d0'' _ (i0, _, S)
d1 _ (d1', _, R)
d1' * (d1', *, R)
d1' _ (d1'', _, R)
d1'' * (d1'', *, R)
d1'' _ (i1, _, S)
i0 _ (b, 0, S)
i1 _ (b, 1, S)
b * (b, *, L)
b _ (b', _, L)
b' * (b', *, L)
b' _ (q1, _, S)
mb _ (k0, _, R)
k0 0 (j0, _, L)
k0 1 (j1, _, L)
k0 _ (H, _, L)
j0 _ (mb, 0, R)
j1 _ (mb, 1, R)
