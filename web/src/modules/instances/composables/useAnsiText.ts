const ANSI_CODES: { [key: string]: string } = {
  "0": "reset",
  "39": "color: rgba(255, 255, 255, .38)",
  "1": "font-weight: bold",
  "2": "opacity: 0.7",
  "3": "font-style: italic",
  "4": "text-decoration: underline",
  "30": "color: #6b7280",
  "31": "color: #fca5a5",
  "32": "color: #86efac",
  "33": "color: #fde68a",
  "34": "color: #93c5fd",
  "35": "color: #f0abfc",
  "36": "color: #a5f3fc",
  "37": "color: #e5e7eb",
  "40": "background-color: #374151; color: #e5e7eb",
  "41": "background-color: #fecaca; color: #7f1d1d",
  "42": "background-color: #bbf7d0; color: #14532d",
  "43": "background-color: #fef3c7; color: #713f12",
  "44": "background-color: #bfdbfe; color: #1e3a8a",
  "45": "background-color: #f5d0fe; color: #701a75",
  "46": "background-color: #cffafe; color: #164e63",
  "47": "background-color: #f3f4f6; color: #1f2937",
  "49": "background-color: rgba(255, 255, 255, .38)",
  "90": "color: #9ca3af",
  "91": "color: #fecaca",
  "92": "color: #bbf7d0",
  "93": "color: #fef3c7",
  "94": "color: #bfdbfe",
  "95": "color: #f5d0fe",
  "96": "color: #cffafe",
  "97": "color: #f3f4f6",
  "100": "background-color: #6b7280; color: #f9fafb",
  "101": "background-color: #fca5a5; color: #7f1d1d",
  "102": "background-color: #86efac; color: #14532d",
  "103": "background-color: #fde68a; color: #713f12",
  "104": "background-color: #93c5fd; color: #1e3a8a",
  "105": "background-color: #f0abfc; color: #701a75",
  "106": "background-color: #a5f3fc; color: #164e63",
  "107": "background-color: #e5e7eb; color: #1f2937"
}

export function useAnsiText(text: string): string {
  let html = text
  let openSpans = 0

  html = html.replace(/\x1b\[(\d+(?:;\d+)*)m/g, (_, codes) => {
    const codeList = codes.split(";")
    const styles = []
    let result = ""

    for (const code of codeList) {
      if (code === "0") {
        result += "</span>".repeat(openSpans)
        openSpans = 0
      } else if (ANSI_CODES[code] && ANSI_CODES[code] !== "reset") {
        styles.push(ANSI_CODES[code])
      }
    }

    if (styles.length > 0) {
      result += `<span style="${styles.join("; ")}">`
      openSpans++
    }

    return result
  })

  if (openSpans > 0) {
    html += "</span>".repeat(openSpans)
  }

  html = html.replace(/&(?!#?\w+;)/g, "&amp;")

  return html
}
