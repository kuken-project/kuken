<template>
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 120 76" fill="none">
    <defs>
      <clipPath id="left-half">
        <rect width="60" height="76" />
      </clipPath>
      <clipPath id="right-half">
        <rect x="60" width="60" height="76" />
      </clipPath>
      <clipPath id="rounded">
        <rect width="120" height="76" rx="6" />
      </clipPath>
    </defs>

    <g clip-path="url(#rounded)">
      <!-- ── Left half (Light) ── -->
      <g clip-path="url(#left-half)">
        <rect width="120" height="76" fill="#f0eeec" />
        <rect width="120" height="16" fill="#e8703a" />
        <rect x="6" y="22" width="30" height="48" rx="4" fill="#ffffff" />
        <rect x="42" y="22" width="72" height="48" rx="4" fill="#ffffff" />
        <rect x="10" y="28" width="22" height="3" rx="1.5" fill="#e9ecef" />
        <rect x="10" y="35" width="18" height="3" rx="1.5" fill="#e9ecef" />
        <rect x="10" y="42" width="20" height="3" rx="1.5" fill="#e9ecef" />
        <rect x="48" y="28" width="40" height="3" rx="1.5" fill="#e9ecef" />
        <rect x="48" y="35" width="56" height="3" rx="1.5" fill="#e9ecef" />
        <rect x="48" y="42" width="48" height="3" rx="1.5" fill="#e9ecef" />
        <rect x="48" y="52" width="30" height="8" rx="4" fill="#fdebd4" />
        <rect x="82" y="52" width="26" height="8" rx="4" fill="#fdebd4" />
      </g>

      <!-- ── Right half (Dark) ── -->
      <g clip-path="url(#right-half)">
        <rect width="120" height="76" fill="#171723" />
        <rect width="120" height="16" fill="#b84520" />
        <rect x="6" y="22" width="30" height="48" rx="4" fill="#22223a" />
        <rect x="42" y="22" width="72" height="48" rx="4" fill="#22223a" />
        <rect x="10" y="28" width="22" height="3" rx="1.5" fill="#2e2e4a" />
        <rect x="10" y="35" width="18" height="3" rx="1.5" fill="#2e2e4a" />
        <rect x="10" y="42" width="20" height="3" rx="1.5" fill="#2e2e4a" />
        <rect x="48" y="28" width="40" height="3" rx="1.5" fill="#2e2e4a" />
        <rect x="48" y="35" width="56" height="3" rx="1.5" fill="#2e2e4a" />
        <rect x="48" y="42" width="48" height="3" rx="1.5" fill="#2e2e4a" />
        <rect x="48" y="52" width="30" height="8" rx="4" fill="#332a24" />
        <rect x="82" y="52" width="26" height="8" rx="4" fill="#332a24" />
      </g>

      <!-- Divider line -->
      <line x1="60" y1="0" x2="60" y2="76" stroke="#ffffff" stroke-width="1" opacity="0.3" />
    </g>
  </svg>
</template>
