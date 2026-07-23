(() => {
    const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    if (!window.gsap || reduceMotion) {
        return;
    }

    gsap.registerPlugin(ScrollTrigger);
    gsap.defaults({ ease: "power3.out", duration: 0.8 });

    const header = document.querySelector(".site-header");
    if (header) {
        const toggleScrolled = () => {
            if (window.scrollY > 20) {
                header.classList.add("is-scrolled");
            } else {
                header.classList.remove("is-scrolled");
            }
        };
        window.addEventListener("scroll", toggleScrolled, { passive: true });
        toggleScrolled();

        ScrollTrigger.create({
            trigger: document.body,
            start: "top top-=24",
            end: "bottom top",
            toggleClass: {
                targets: header,
                className: "is-scrolled"
            }
        });
    }

    const heroTimeline = gsap.timeline({ defaults: { ease: "power3.out" } });
    heroTimeline
        .from("[data-animate='nav']", { y: -18, autoAlpha: 0, duration: 0.55 })
        .from(".hero-image", { autoAlpha: 0, duration: 0.65 }, "<")
        .from("[data-animate='hero-copy']", {
            y: 28,
            autoAlpha: 0,
            stagger: 0.09,
            duration: 0.72
        }, "-=0.55");

    gsap.utils.toArray(".reveal-block").forEach((element) => {
        gsap.from(element, {
            y: 28,
            autoAlpha: 0,
            immediateRender: false,
            scrollTrigger: {
                trigger: element,
                start: "top 82%",
                toggleActions: "play none none reverse"
            }
        });
    });

    ScrollTrigger.batch(".reveal-card", {
        start: "top 86%",
        batchMax: 4,
        interval: 0.08,
        onEnter: (batch) => {
            gsap.from(batch, {
                y: 34,
                autoAlpha: 0,
                immediateRender: false,
                stagger: 0.08,
                overwrite: true
            });
        },
        onLeaveBack: (batch) => {
            gsap.to(batch, {
                y: 0,
                autoAlpha: 1,
                duration: 0.35,
                overwrite: true
            });
        }
    });

    document.querySelectorAll(".retail-btn").forEach((button) => {
        button.addEventListener("mouseenter", () => {
            gsap.to(button, { y: -2, duration: 0.22, overwrite: "auto" });
        });
        button.addEventListener("mouseleave", () => {
            gsap.to(button, { y: 0, duration: 0.22, overwrite: "auto" });
        });
    });

    window.addEventListener("load", () => ScrollTrigger.refresh(), { once: true });
})();
