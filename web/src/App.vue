<script setup>
import { ref, computed, onMounted } from 'vue'

const GITHUB = 'https://github.com/imdotrino/dotrino-android-launcher'
const RELEASES = GITHUB + '/releases/latest'
const DISCORD = 'https://discord.gg/D648uq7cth'

/* ---------------- i18n (es/en · tuteo, sin voseo · lenguaje llano) ---------------- */
const I18N = {
  es: {
    nav_how: 'Cómo funciona', nav_download: 'Descargar',
    hero_kicker: 'Launcher de Android · gratis y de código abierto',
    hero_title: 'Tu pantalla de inicio, ordenada y privada',
    hero_sub: 'Un launcher que pone tus apps en orden de la A a la Z y te deja esconder las que quieras detrás de tu huella. Reemplaza la pantalla de inicio de tu Android, sin anuncios y sin rastreo.',
    hero_download: 'Descargar APK', hero_source: 'Ver el código',
    hero_note: 'Para Android 8 o superior. Gratis.',
    why_title: '¿Para qué sirve?',
    why_body: 'La pantalla de inicio de muchos teléfonos es un revoltijo de apps, y cualquiera que agarra tu teléfono ve todo lo que tienes. Este launcher pone tus apps en orden alfabético —con un índice al costado para saltar rápido a la letra que buscas— y te deja esconder las apps privadas detrás de tu huella, tu cara o tu patrón, con un acceso disimulado. Tu teléfono, más ordenado y más tuyo.',
    how_title: 'Cómo funciona',
    how_1_t: 'Descárgalo e instálalo',
    how_1_b: 'Bajas el archivo (APK) y lo abres en tu teléfono. Como no viene de la tienda, Android te pedirá permiso para instalarlo: acéptalo. Tarda un momento.',
    how_2_t: 'Ponlo como pantalla de inicio',
    how_2_b: 'Al tocar el botón de inicio, Android te preguntará qué launcher usar. Elige Dotrino Launcher y, si quieres, marca "siempre".',
    how_3_t: 'Esconde lo que quieras',
    how_3_b: 'Entra en la configuración del launcher, elige qué apps ocultar y protégelas con tu huella o patrón. Quedan fuera de la vista.',
    feat_title: 'Lo que te da',
    feats: [
      ['Ordenado de la A a la Z', 'Tus apps por letra, con un índice al costado para saltar a la que buscas sin dar mil vueltas.'],
      ['Esconde apps', 'Pon las apps privadas detrás de tu huella, tu cara o tu patrón. El acceso va disimulado.'],
      ['Recientes a mano', 'Tus últimas apps usadas, siempre arriba, sin tener que buscarlas.'],
      ['Lo básico, fijo', 'Teléfono y cámara siempre abajo, a un toque.'],
      ['Privado', 'Sin anuncios, sin rastreo y sin pedir permisos raros. Tu uso no le importa a nadie más.'],
      ['Gratis y abierto', 'No cuesta nada, no pide cuenta, y su código es abierto para que cualquiera lo revise.'],
    ],
    honest_t: 'Con honestidad',
    honest_b: 'Un launcher solo manda en su propia pantalla: las apps escondidas siguen instaladas y alguien podría encontrarlas en los Ajustes del teléfono o con un buscador. Sirve para mantenerlas fuera de la vista en el día a día, no para borrarlas del sistema.',
    dl_title: 'Descarga',
    dl_lead: 'Gratis y sin tienda de por medio: descargas el archivo y lo instalas tú.',
    dl_btn: 'Descargar APK',
    dl_steps_t: 'Cómo instalarlo',
    dl_step1: 'Toca "Descargar APK", baja el archivo .apk de la página y ábrelo cuando termine.',
    dl_step2: 'Tu teléfono te pedirá permiso para instalar desde esta fuente: acéptalo (es normal en apps de fuera de la tienda).',
    dl_step3: 'Ábrelo y elígelo como tu pantalla de inicio. Listo.',
    dl_note: 'Para Android 8 o superior.',
    foot_tag: 'Tu teléfono, ordenado y bajo tus reglas.',
    foot_eco: 'Parte del ecosistema Dotrino', foot_src: 'Código', foot_discord: 'Discord',
  },
  en: {
    nav_how: 'How it works', nav_download: 'Download',
    hero_kicker: 'Android launcher · free and open source',
    hero_title: 'Your home screen, tidy and private',
    hero_sub: 'A launcher that puts your apps in A-to-Z order and lets you hide the ones you want behind your fingerprint. It replaces your Android home screen, with no ads and no tracking.',
    hero_download: 'Download APK', hero_source: 'View the code',
    hero_note: 'For Android 8 or newer. Free.',
    why_title: 'What is it for?',
    why_body: 'Many phones have a messy home screen, and anyone who grabs your phone sees everything you have. This launcher puts your apps in alphabetical order —with a side index to jump quickly to the letter you want— and lets you hide private apps behind your fingerprint, face or pattern, through a disguised shortcut. Your phone, tidier and more yours.',
    how_title: 'How it works',
    how_1_t: 'Download and install it',
    how_1_b: 'You download the file (APK) and open it on your phone. Since it’s not from the store, Android will ask permission to install it: accept. It takes a moment.',
    how_2_t: 'Set it as your home screen',
    how_2_b: 'When you tap the home button, Android asks which launcher to use. Pick Dotrino Launcher and, if you want, choose "always".',
    how_3_t: 'Hide whatever you want',
    how_3_b: 'Go into the launcher settings, pick which apps to hide and protect them with your fingerprint or pattern. They stay out of sight.',
    feat_title: 'What you get',
    feats: [
      ['Sorted A to Z', 'Your apps by letter, with a side index to jump to the one you want without endless scrolling.'],
      ['Hide apps', 'Put private apps behind your fingerprint, face or pattern. The shortcut is disguised.'],
      ['Recents at hand', 'Your last-used apps, always on top, without searching.'],
      ['The basics, fixed', 'Phone and camera always at the bottom, one tap away.'],
      ['Private', 'No ads, no tracking and no weird permissions. What you do is nobody else’s business.'],
      ['Free and open', 'It costs nothing, asks for no account, and its code is open for anyone to review.'],
    ],
    honest_t: 'Honestly',
    honest_b: 'A launcher only rules its own screen: hidden apps are still installed and someone could find them in the phone’s Settings or via search. It keeps them out of sight day to day, not removed from the system.',
    dl_title: 'Download',
    dl_lead: 'Free and no store in between: you download the file and install it yourself.',
    dl_btn: 'Download APK',
    dl_steps_t: 'How to install it',
    dl_step1: 'Tap "Download APK", grab the .apk file from the page and open it when it finishes.',
    dl_step2: 'Your phone will ask permission to install from this source: accept (it’s normal for apps from outside the store).',
    dl_step3: 'Open it and set it as your home screen. Done.',
    dl_note: 'For Android 8 or newer.',
    foot_tag: 'Your phone, tidy and under your rules.',
    foot_eco: 'Part of the Dotrino ecosystem', foot_src: 'Source', foot_discord: 'Discord',
  },
}
const LANG_KEY = 'launcher.lang'
const lang = ref((localStorage.getItem(LANG_KEY) || (navigator.language || 'es').slice(0, 2)) === 'en' ? 'en' : 'es')
const t = computed(() => I18N[lang.value])
const setLang = (l) => { lang.value = l; localStorage.setItem(LANG_KEY, l); document.documentElement.lang = l }

onMounted(() => { document.documentElement.lang = lang.value })
</script>

<template>
  <div class="page">
    <header class="topbar">
      <a class="brand" href="/"><img src="/icon.svg" alt="" width="30" height="30" /><span>Dotrino&nbsp;Launcher</span></a>
      <nav class="navlinks">
        <a href="#how">{{ t.nav_how }}</a>
        <a href="#download">{{ t.nav_download }}</a>
      </nav>
      <div class="actions">
        <div class="lang-selector" role="group" aria-label="es / en">
          <button :class="{ on: lang === 'es' }" @click="setLang('es')">ES</button>
          <button :class="{ on: lang === 'en' }" @click="setLang('en')">EN</button>
        </div>
        <dotrino-support href="https://ko-fi.com/dotrino" repo="imdotrino/dotrino-android-launcher" :discord="DISCORD" :lang="lang"></dotrino-support>
      </div>
    </header>

    <main>
      <section class="hero">
        <p class="kicker">{{ t.hero_kicker }}</p>
        <h1>{{ t.hero_title }}</h1>
        <p class="lead">{{ t.hero_sub }}</p>
        <div class="cta">
          <a class="btn btn-primary" href="#download" data-testid="hero-download">↓ {{ t.hero_download }}</a>
          <a class="btn btn-ghost" :href="GITHUB">{{ t.hero_source }}</a>
        </div>
        <p class="note">{{ t.hero_note }}</p>
        <div class="shield" aria-hidden="true"><img src="/icon.svg" alt="" width="150" height="150" /></div>
      </section>

      <section class="why">
        <h2>{{ t.why_title }}</h2>
        <p>{{ t.why_body }}</p>
      </section>

      <section id="how" class="how">
        <h2>{{ t.how_title }}</h2>
        <ol class="steps">
          <li><span class="num">1</span><div><h3>{{ t.how_1_t }}</h3><p>{{ t.how_1_b }}</p></div></li>
          <li><span class="num">2</span><div><h3>{{ t.how_2_t }}</h3><p>{{ t.how_2_b }}</p></div></li>
          <li><span class="num">3</span><div><h3>{{ t.how_3_t }}</h3><p>{{ t.how_3_b }}</p></div></li>
        </ol>
      </section>

      <section class="features">
        <h2>{{ t.feat_title }}</h2>
        <div class="grid">
          <div class="feat" v-for="(f, i) in t.feats" :key="i">
            <h3>{{ f[0] }}</h3><p>{{ f[1] }}</p>
          </div>
        </div>
        <div class="honest">
          <h3>{{ t.honest_t }}</h3><p>{{ t.honest_b }}</p>
        </div>
      </section>

      <section id="download" class="download">
        <h2>{{ t.dl_title }}</h2>
        <p class="lead">{{ t.dl_lead }}</p>
        <a class="btn btn-primary btn-lg" :href="RELEASES" data-testid="download-apk">↓ {{ t.dl_btn }}</a>

        <div class="install">
          <h3>{{ t.dl_steps_t }}</h3>
          <ol>
            <li>{{ t.dl_step1 }}</li>
            <li>{{ t.dl_step2 }}</li>
            <li>{{ t.dl_step3 }}</li>
          </ol>
          <p class="note">{{ t.dl_note }}</p>
        </div>
      </section>
    </main>

    <footer class="foot">
      <p class="foot-tag">{{ t.foot_tag }}</p>
      <nav class="foot-links">
        <a :href="GITHUB">{{ t.foot_src }}</a>
        <a :href="DISCORD">{{ t.foot_discord }}</a>
        <a href="https://dotrino.com">{{ t.foot_eco }}</a>
      </nav>
      <p class="foot-lic">MIT · Dotrino</p>
    </footer>
  </div>
</template>
