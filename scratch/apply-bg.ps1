$templatesDir = "d:\ThucTapCuoikhoa\perfume-thesis\src\main\resources\templates"
$files = Get-ChildItem -Path $templatesDir -Filter "*.html" | Where-Object { $_.Name -notin @("index.html", "detail.html", "products.html") }

$cssInsert = @"

        /* ==================== LUXURY FLOATING BACKGROUND ==================== */
        .floating-bg {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            pointer-events: none;
            z-index: 999;
            overflow: hidden;
        }

        .floating-particle {
            position: absolute;
            bottom: -60px;
            pointer-events: none;
            opacity: 0;
            will-change: transform, opacity;
        }

        /* Silver bokeh orbs — bright shimmering */
        .floating-particle.bokeh {
            border-radius: 50%;
            background: radial-gradient(circle at 35% 35%, 
                rgba(220, 230, 255, 0.45) 0%, 
                rgba(200, 215, 240, 0.2) 35%, 
                rgba(180, 200, 230, 0.05) 65%,
                transparent 80%);
            box-shadow: 
                inset 0 0 20px rgba(220, 230, 255, 0.2),
                0 0 30px rgba(200, 220, 255, 0.15),
                0 0 60px rgba(180, 200, 245, 0.06);
        }

        /* Elegant petals — visible silver shimmer */
        .floating-particle.petal {
            border-radius: 80% 0 55% 50% / 55% 0 80% 50%;
            background: linear-gradient(135deg, 
                rgba(210, 220, 245, 0.5) 0%, 
                rgba(190, 205, 235, 0.25) 50%, 
                rgba(175, 190, 225, 0.08) 100%);
            border: 1px solid rgba(220, 230, 255, 0.15);
            box-shadow: 0 0 12px rgba(200, 215, 250, 0.1);
        }

        /* Diamond sparkle — brilliant white-silver glints */
        .floating-particle.sparkle {
            background: rgba(240, 245, 255, 1);
            border-radius: 50%;
            box-shadow: 
                0 0 6px rgba(220, 230, 255, 0.9),
                0 0 15px rgba(200, 215, 250, 0.6),
                0 0 30px rgba(180, 200, 245, 0.3),
                0 0 45px rgba(160, 185, 240, 0.1);
        }

        /* Ambient mist — visible cool silver glow */
        .floating-particle.mist {
            border-radius: 50%;
            background: radial-gradient(ellipse at center,
                rgba(160, 180, 230, 0.1) 0%,
                rgba(180, 200, 240, 0.05) 45%,
                transparent 70%);
            filter: blur(25px);
        }

        /* Silver thread — visible shimmering lines */
        .floating-particle.thread {
            width: 1px;
            background: linear-gradient(to bottom,
                transparent 0%,
                rgba(210, 220, 250, 0.3) 25%,
                rgba(230, 240, 255, 0.5) 50%,
                rgba(210, 220, 250, 0.3) 75%,
                transparent 100%);
            border-radius: 1px;
            box-shadow: 0 0 6px rgba(200, 215, 250, 0.2);
        }

        @keyframes floatUpLuxury {
            0% {
                transform: translateY(0) translateX(0) rotate(0deg);
                opacity: 0;
            }
            8% {
                opacity: var(--max-opacity, 0.7);
            }
            25% {
                transform: translateY(-25vh) translateX(var(--sway-1, 20px)) rotate(45deg);
            }
            50% {
                opacity: var(--mid-opacity, 0.5);
                transform: translateY(-50vh) translateX(var(--sway-2, -15px)) rotate(120deg);
            }
            75% {
                transform: translateY(-75vh) translateX(var(--sway-3, 10px)) rotate(200deg);
                opacity: var(--fade-opacity, 0.25);
            }
            100% {
                transform: translateY(-110vh) translateX(var(--sway-4, -5px)) rotate(300deg);
                opacity: 0;
            }
        }

        @keyframes driftSlow {
            0%, 100% { transform: translateX(0) rotate(0deg); }
            33% { transform: translateX(var(--drift-x, 15px)) rotate(var(--drift-r, 3deg)); }
            66% { transform: translateX(calc(var(--drift-x, 15px) * -0.7)) rotate(calc(var(--drift-r, 3deg) * -1)); }
        }

        @keyframes pulseSparkle {
            0%, 100% { opacity: 0.3; transform: scale(0.8); }
            50% { opacity: 1; transform: scale(1.5); }
        }

        @keyframes mistDrift {
            0% { transform: translate(0, 0) scale(1); opacity: 0; }
            20% { opacity: var(--mist-opacity, 0.08); }
            50% { transform: translate(var(--mist-x, 50px), -30vh) scale(1.2); }
            80% { opacity: var(--mist-opacity, 0.08); }
            100% { transform: translate(var(--mist-x2, -30px), -60vh) scale(0.8); opacity: 0; }
        }
    </style>
"@

$htmlInsert = @"
<body>
    <!-- ==================== FLOATING BACKGROUND ==================== -->
    <div class="floating-bg" id="floatingBg"></div>

    <script>
    (function() {
        const container = document.getElementById('floatingBg');

        function rand(min, max) { return min + Math.random() * (max - min); }
        function randSign() { return Math.random() > 0.5 ? 1 : -1; }

        // --- COOL SILVER BOKEH ORBS ---
        for (let i = 0; i < 6; i++) {
            const el = document.createElement('div');
            el.classList.add('floating-particle', 'bokeh');
            const size = rand(30, 80);
            el.style.width = size + 'px';
            el.style.height = size + 'px';
            el.style.left = rand(0, 100) + '%';
            el.style.setProperty('--max-opacity', rand(0.3, 0.6).toFixed(2));
            el.style.setProperty('--mid-opacity', rand(0.15, 0.4).toFixed(2));
            el.style.setProperty('--fade-opacity', rand(0.08, 0.2).toFixed(2));
            el.style.setProperty('--sway-1', (rand(15, 40) * randSign()) + 'px');
            el.style.setProperty('--sway-2', (rand(10, 30) * randSign()) + 'px');
            el.style.setProperty('--sway-3', (rand(10, 25) * randSign()) + 'px');
            el.style.setProperty('--sway-4', (rand(5, 15) * randSign()) + 'px');
            const dur = rand(18, 35);
            const delay = rand(0, 15);
            el.style.animation = `floatUpLuxury ${dur}s ${delay}s linear infinite`;
            container.appendChild(el);
        }

        // --- TRANSLUCENT SILVER PETALS ---
        const petalColors = [
            [190, 200, 220],
            [180, 195, 215],
            [200, 210, 230],
            [175, 190, 210],
            [185, 180, 210],
        ];
        for (let i = 0; i < 10; i++) {
            const el = document.createElement('div');
            el.classList.add('floating-particle', 'petal');
            const size = rand(10, 22);
            el.style.width = size + 'px';
            el.style.height = (size * rand(0.7, 1.3)) + 'px';
            el.style.left = rand(0, 100) + '%';
            const c = petalColors[Math.floor(Math.random() * petalColors.length)];
            el.style.background = `linear-gradient(${rand(90, 200)}deg,
                rgba(${c[0]}, ${c[1]}, ${c[2]}, ${rand(0.2, 0.4).toFixed(2)}) 0%,
                rgba(${c[0]}, ${c[1]}, ${c[2]}, ${rand(0.05, 0.15).toFixed(2)}) 100%)`;
            el.style.setProperty('--max-opacity', rand(0.6, 0.9).toFixed(2));
            el.style.setProperty('--mid-opacity', rand(0.35, 0.6).toFixed(2));
            el.style.setProperty('--fade-opacity', rand(0.1, 0.25).toFixed(2));
            el.style.setProperty('--sway-1', (rand(20, 50) * randSign()) + 'px');
            el.style.setProperty('--sway-2', (rand(15, 40) * randSign()) + 'px');
            el.style.setProperty('--sway-3', (rand(10, 35) * randSign()) + 'px');
            el.style.setProperty('--sway-4', (rand(5, 20) * randSign()) + 'px');
            el.style.setProperty('--drift-x', (rand(10, 30) * randSign()) + 'px');
            el.style.setProperty('--drift-r', (rand(5, 15) * randSign()) + 'deg');
            const dur = rand(14, 28);
            const delay = rand(0, 15);
            const driftDur = rand(8, 16);
            el.style.animation = `floatUpLuxury ${dur}s ${delay}s linear infinite, driftSlow ${driftDur}s ${delay}s ease-in-out infinite`;
            container.appendChild(el);
        }

        // --- DIAMOND SPARKLES ---
        for (let i = 0; i < 15; i++) {
            const el = document.createElement('div');
            el.classList.add('floating-particle', 'sparkle');
            const size = rand(3, 6);
            el.style.width = size + 'px';
            el.style.height = size + 'px';
            el.style.left = rand(0, 100) + '%';
            el.style.setProperty('--max-opacity', rand(0.7, 1).toFixed(2));
            el.style.setProperty('--mid-opacity', rand(0.4, 0.7).toFixed(2));
            el.style.setProperty('--fade-opacity', rand(0.15, 0.35).toFixed(2));
            el.style.setProperty('--sway-1', (rand(5, 20) * randSign()) + 'px');
            el.style.setProperty('--sway-2', (rand(5, 15) * randSign()) + 'px');
            el.style.setProperty('--sway-3', (rand(3, 12) * randSign()) + 'px');
            el.style.setProperty('--sway-4', (rand(2, 8) * randSign()) + 'px');
            const dur = rand(10, 25);
            const delay = rand(0, 15);
            const pulseDur = rand(2, 5);
            el.style.animation = `floatUpLuxury ${dur}s ${delay}s linear infinite, pulseSparkle ${pulseDur}s ${delay}s ease-in-out infinite`;
            container.appendChild(el);
        }
    })();
    </script>
"@

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

foreach ($file in $files) {
    # Sử dụng .NET System.IO.File để đọc file với mã UTF-8 chính xác
    $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
    
    if ($content -notlike "*floating-bg*") {
        Write-Host "Ghi đè UTF-8 file: $($file.Name)"
        
        $content = $content.Replace("</style>", $cssInsert)
        $content = $content.Replace("<body>", $htmlInsert)
        
        # Ghi đè file với UTF-8 không có BOM (hoặc có BOM) bằng phương thức .NET an toàn
        [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
    }
}
Write-Host "Xử lý thành công!"
