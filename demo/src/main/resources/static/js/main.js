// Inizializzazione quando il DOM è pronto
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM Caricato - Inizializzazione sito matrimonio');

    // ========== GESTIONE NAVIGAZIONE ARTICOLI ==========
    const body = document.body;
    const main = document.getElementById('main');
    const articles = document.querySelectorAll('.article');
    const header = document.getElementById('header');
    const footer = document.getElementById('footer');
    let locked = false;
    const delay = 325;

    function showArticle(id, initial = false) {
        const targetArticle = document.getElementById(id);
        if (!targetArticle) {
            console.error('Articolo non trovato:', id);
            return;
        }

        console.log('Mostro articolo:', id);

        if (locked || initial) {
            body.classList.add('is-switching');
            body.classList.add('is-article-visible');
            articles.forEach(art => art.classList.remove('active'));
            header.style.display = 'none';
            footer.style.display = 'none';
            main.style.display = 'flex';
            targetArticle.style.display = 'block';
            targetArticle.classList.add('active');
            locked = false;
            setTimeout(() => body.classList.remove('is-switching'), initial ? 1000 : 0);
            window.scrollTo(0,0);
            return;
        }

        locked = true;

        if (body.classList.contains('is-article-visible')) {
            const current = document.querySelector('.article.active');
            if (current) current.classList.remove('active');
            setTimeout(() => {
                if (current) current.style.display = 'none';
                targetArticle.style.display = 'block';
                setTimeout(() => {
                    targetArticle.classList.add('active');
                    window.scrollTo(0,0);
                    setTimeout(() => { locked = false; }, delay);
                }, 25);
            }, delay);
        } else {
            body.classList.add('is-article-visible');
            setTimeout(() => {
                header.style.display = 'none';
                footer.style.display = 'none';
                main.style.display = 'flex';
                targetArticle.style.display = 'block';
                setTimeout(() => {
                    targetArticle.classList.add('active');
                    window.scrollTo(0,0);
                    setTimeout(() => { locked = false; }, delay);
                }, 25);
            }, delay);
        }
    }

    function hideArticle(addState = false) {
        if (!body.classList.contains('is-article-visible')) return;
        if (addState && history.pushState) {
            history.pushState(null, null, '#');
        }
        const activeArticle = document.querySelector('.article.active');
        if (!activeArticle) return;

        console.log('Nascondo articolo');
        locked = true;
        activeArticle.classList.remove('active');
        setTimeout(() => {
            activeArticle.style.display = 'none';
            main.style.display = 'none';
            header.style.display = 'flex';
            footer.style.display = 'block';
            setTimeout(() => {
                body.classList.remove('is-article-visible');
                window.scrollTo(0,0);
                setTimeout(() => { locked = false; }, delay);
            }, 25);
        }, delay);
    }

    // Attach event listeners per navigazione
    const navStory = document.getElementById('nav-story');
    const navWedding = document.getElementById('nav-wedding');
    const navGallery = document.getElementById('nav-gallery');
    const navRsvp = document.getElementById('nav-rsvp');
    const navGifts = document.getElementById('nav-gifts');

    if (navStory) navStory.addEventListener('click', (e) => { e.preventDefault(); showArticle('story'); });
    if (navWedding) navWedding.addEventListener('click', (e) => { e.preventDefault(); showArticle('wedding'); });
    if (navGallery) navGallery.addEventListener('click', (e) => { e.preventDefault(); showArticle('gallery'); });
    if (navRsvp) navRsvp.addEventListener('click', (e) => { e.preventDefault(); showArticle('rsvp'); });
    if (navGifts) navGifts.addEventListener('click', (e) => { e.preventDefault(); showArticle('gifts'); });

    document.querySelectorAll('.close').forEach(btn => {
        btn.addEventListener('click', () => hideArticle(true));
    });

    document.body.addEventListener('click', (e) => {
        if (body.classList.contains('is-article-visible') && !e.target.closest('.article') && !e.target.closest('nav a')) {
            hideArticle(true);
        }
    });

    window.addEventListener('keyup', (e) => {
        if (e.key === 'Escape' && body.classList.contains('is-article-visible')) {
            hideArticle(true);
        }
    });

    if (window.location.hash && window.location.hash !== '#') {
        const hashId = window.location.hash.substring(1);
        if (document.getElementById(hashId)) {
            setTimeout(() => showArticle(hashId, true), 100);
        }
    }

    window.addEventListener('hashchange', () => {
        if (window.location.hash === '' || window.location.hash === '#') {
            hideArticle();
        } else {
            const id = window.location.hash.substring(1);
            if (document.getElementById(id)) showArticle(id);
        }
    });

    // ========== CONTROLLO DATE ALLOGGIO ==========
    const needAccommodationCheckbox = document.getElementById('needAccommodation');
    const accommodationDetails = document.getElementById('accommodationDetails');
    const dateFrom = document.getElementById('dateFrom');
    const dateTo = document.getElementById('dateTo');
    const dateError = document.getElementById('dateError');

    function updateDateLimits() {
        if (!dateFrom || !dateTo) return;

        const fromValue = dateFrom.value;
        const toValue = dateTo.value;

        if (fromValue) {
            const minDeparture = new Date(fromValue);
            minDeparture.setDate(minDeparture.getDate() + 1);
            const minDepartureStr = minDeparture.toISOString().split('T')[0];
            dateTo.min = minDepartureStr;

            if (toValue && new Date(toValue) <= new Date(fromValue)) {
                dateTo.value = minDepartureStr;
            } else if (!toValue) {
                dateTo.value = minDepartureStr;
            }
        }

        if (toValue) {
            const maxArrival = new Date(toValue);
            maxArrival.setDate(maxArrival.getDate() - 1);
            const maxArrivalStr = maxArrival.toISOString().split('T')[0];
            dateFrom.max = maxArrivalStr;

            if (fromValue && new Date(fromValue) >= new Date(toValue)) {
                dateFrom.value = maxArrivalStr;
            }
        }
    }

    function validateDates() {
        if (!dateFrom || !dateTo) return true;

        const fromDate = new Date(dateFrom.value);
        const toDate = new Date(dateTo.value);
        const isValid = toDate > fromDate;

        if (!isValid && dateError) {
            dateError.classList.add('show');
        } else if (dateError) {
            dateError.classList.remove('show');
        }

        return isValid;
    }

    if (dateFrom) {
        dateFrom.addEventListener('change', function() {
            updateDateLimits();
            validateDates();
        });
    }

    if (dateTo) {
        dateTo.addEventListener('change', function() {
            updateDateLimits();
            validateDates();
        });
    }

    function initializeDateLimits() {
        updateDateLimits();
        validateDates();
    }

    if (needAccommodationCheckbox) {
        needAccommodationCheckbox.addEventListener('change', function() {
            if (accommodationDetails) {
                accommodationDetails.style.display = this.checked ? 'block' : 'none';
            }
            if (this.checked) {
                initializeDateLimits();
            }
        });
    }

    // ========== SUBMIT FORM RSVP  ==========
    const rsvpForm = document.getElementById('rsvpForm');

    if (rsvpForm) {
        console.log('Form RSVP trovato, attacco event listener');

        rsvpForm.addEventListener('submit', function(e) {
            e.preventDefault();
            e.stopPropagation();

            console.log('=== FORM SUBMIT INIZIATO ===');

            // --- ELEMENTI UI PER GESTIRE IL CARICAMENTO ---
            const submitButton = document.getElementById('submitRSVP');
            const feedback = document.getElementById('formFeedback');
            const fieldsContainer = rsvpForm.querySelector('.fields');
            const actionsContainer = rsvpForm.querySelector('.actions');

            // Prendi i valori per la validazione
            const name = document.getElementById('name').value.trim();
            const email = document.getElementById('email').value.trim();
            const needAccommodation = document.getElementById('needAccommodation').checked;

            console.log('Nome:', name);
            console.log('Email:', email);

            // Validazione base campi obbligatori
            if (!name || !email) {
                if (feedback) {
                    feedback.innerHTML = '<span style="color: #ffaaaa;"><i class="fas fa-exclamation-triangle"></i> Per favore compila nome ed email.</span>';
                    setTimeout(() => { feedback.innerHTML = ''; }, 4000);
                }
                return;
            }

            // Validazione formato email
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(email)) {
                if (feedback) {
                    feedback.innerHTML = '<span style="color: #ffaaaa;"><i class="fas fa-exclamation-triangle"></i> Inserisci un indirizzo email valido.</span>';
                    setTimeout(() => { feedback.innerHTML = ''; }, 4000);
                }
                return;
            }

            // Validazione date alloggio
            if (needAccommodation) {
                const isDateValid = validateDates();
                if (!isDateValid) {
                    if (feedback) {
                        feedback.innerHTML = '<span style="color: #ffaaaa;"><i class="fas fa-exclamation-triangle"></i> La data di partenza deve essere dopo la data di arrivo (almeno una notte).</span>';
                        setTimeout(() => { feedback.innerHTML = ''; }, 4000);
                    }
                    return;
                }
            }

            // Raccogli tutti i dati finali da mandare al backend
            const adults = document.getElementById('adults').value;
            const children = document.getElementById('children').value;
            const message = document.getElementById('message').value;

            const dataToSend = {
                nomeCognome: name,
                email: email,
                nadulti: parseInt(adults) || 0,
                nbambini: parseInt(children) || 0,
                alloggio: needAccommodation,
                nalloggioadulti: needAccommodation ? (parseInt(document.getElementById('accAdults').value) || 0) : 0,
                nalloggiobambini: needAccommodation ? (parseInt(document.getElementById('accChildren').value) || 0) : 0,
                arrivo: needAccommodation ? document.getElementById('dateFrom').value : null,
                partenza: needAccommodation ? document.getElementById('dateTo').value : null,
                note: message
            };

            console.log('Dati raccolti:', dataToSend);

            // disabilita pulsante e avvio del loader
            if (submitButton) {
                submitButton.disabled = true;
            }

            const loaderOverlay = document.createElement('div');
            loaderOverlay.id = 'rsvpLoaderOverlay';
            loaderOverlay.style.position = 'absolute';
            loaderOverlay.style.top = '0';
            loaderOverlay.style.left = '0';
            loaderOverlay.style.width = '100%';
            loaderOverlay.style.height = '100%';
            loaderOverlay.style.backgroundColor = 'rgba(36, 41, 67, 0.9)';
            loaderOverlay.style.display = 'flex';
            loaderOverlay.style.flexDirection = 'column';
            loaderOverlay.style.justifyContent = 'center';
            loaderOverlay.style.alignItems = 'center';
            loaderOverlay.style.zIndex = '10';
            loaderOverlay.style.borderRadius = '8px';

            loaderOverlay.innerHTML = `
                <i class="fas fa-circle-notch fa-spin" style="font-size: 3rem; color: #30c39b; margin-bottom: 1rem;"></i>
                <span style="color: #ffffff; font-weight: 600; font-size: 1.2rem;">Stiamo salvando la tua presenza...</span>
                <p style="color: rgba(255,255,255,0.6); font-size: 0.9rem; margin-top: 0.5rem;">Un attimo di pazienza ⏳</p>
            `;

            rsvpForm.style.position = 'relative';
            rsvpForm.appendChild(loaderOverlay);

            fetch('/presenze', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dataToSend)
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('errore in corso, ritentare');
                    }
                    return response.json();
                })

            // riattiva se ti serve per testare stile
            // new Promise((resolve) => setTimeout(() => resolve(dataToSend), 3000))
                .then(savedPresenza => {
                    console.log('Salvataggio simulato riuscito:', savedPresenza);

                    // rimozione loader dato che server ha risposto
                    const overlay = document.getElementById('rsvpLoaderOverlay');
                    if (overlay) overlay.remove();

                    // nascondere campi form
                    if (fieldsContainer) fieldsContainer.style.display = 'none';
                    if (actionsContainer) actionsContainer.style.display = 'none';

                    // Costruzione del riepilogo di successo coordinato graficamente
                    let messageText = '<div style="background: rgba(48, 195, 155, 0.15); padding: 1.5rem; border-radius: 8px; border: 1px solid #30c39b; margin-top: 1rem; text-align: center;">';
                    messageText += '<h3 style="color: #30c39b; margin-bottom: 0.5rem;"><i class="fas fa-check-circle"></i> Grazie, presenza ricevuta!</h3>';
                    messageText += '<p style="color: #ffffff; font-size: 0.95rem; margin-bottom: 1rem;">Ciao <strong>' + dataToSend.nomeCognome + '</strong>, abbiamo registrato i dettagli del tuo gruppo:</p>';
                    messageText += '<ul style="text-align: left; font-size: 0.85rem; margin-bottom: 1.5rem; list-style: none; padding-left: 0; line-height: 1.6rem; max-width: 320px; margin-left: auto; margin-right: auto;">';
                    messageText += '<li>👥 <strong>Invitati:</strong> ' + dataToSend.nadulti + ' adulto/i e ' + dataToSend.nbambini + ' bambino/i</li>';

                    if (dataToSend.alloggio) {
                        const accTotal = dataToSend.nalloggioadulti + dataToSend.nalloggiobambini;
                        messageText += '<li>🏠 <strong>Richiesta alloggio:</strong> Sì (' + accTotal + ' persone)</li>';

                        const dataArrivoFormattata = dataToSend.arrivo ? dataToSend.arrivo.split('-').reverse().join('/') : '';
                        const dataPartenzaFormattata = dataToSend.partenza ? dataToSend.partenza.split('-').reverse().join('/') : '';
                        messageText += '<li>📅 <strong>Periodo:</strong> dal ' + dataArrivoFormattata + ' al ' + dataPartenzaFormattata + '</li>';
                    } else {
                        messageText += '<li>🏠 <strong>Richiesta alloggio:</strong> No, autonomo</li>';
                    }

                    if (dataToSend.note) {
                        messageText += '<li>💬 <strong>Note/Allergie:</strong> "' + dataToSend.note + '"</li>';
                    }
                    messageText += '</ul>';
                    messageText += '<p style="font-size: 1.05rem; font-weight: 600; color: #30c39b;">Ti abbiamo inviato un\'email di conferma automatica! ✉️</p>';
                    messageText += '<p style="font-size: 1.1rem; font-weight: 600; margin-top: 1rem; margin-bottom: 1.5rem;">✨ Non vediamo l\'ora di festeggiare con voi! ✨</p>';

                    // due bottoni
                    messageText += '<div style="display: flex; gap: 10px; justify-content: center; flex-wrap: wrap;">';
                    messageText += '  <button id="btnRitornaForm" type="button" style="background: #30c39b; color: #ffffff; border: none; padding: 0.6rem 1.2rem; border-radius: 4px; font-weight: 600; cursor: pointer; transition: all 0.3s;">Nuova presenza 👥</button>';
                    messageText += '  <button id="btnTornaHome" type="button" style="background: transparent; color: #ffffff; border: 1px solid rgba(255,255,255,0.4); padding: 0.6rem 1.2rem; border-radius: 4px; font-weight: 600; cursor: pointer; transition: all 0.3s;">Torna alla Home 🏠</button>';
                    messageText += '</div>';
                    messageText += '</div>';
                    if (feedback) feedback.innerHTML = messageText;

                    const btnRitorna = document.getElementById('btnRitornaForm');
                    const btnHome = document.getElementById('btnTornaHome');

                    if (btnRitorna) {
                        btnRitorna.addEventListener('click', function(e) {
                            e.stopPropagation(); // BLOCCA IL BUBBLING
                            feedback.innerHTML = '';
                            fieldsContainer.style.display = 'block';
                            actionsContainer.style.display = 'block';
                            submitButton.disabled = false;
                            rsvpForm.reset();
                        });
                    }

                    if (btnHome) {
                        btnHome.addEventListener('click', function(e) {
                            e.stopPropagation(); // BLOCCA IL BUBBLING
                            hideArticle(true);
                            // Ripristino stato dopo chiusura
                            setTimeout(() => {
                                feedback.innerHTML = '';
                                fieldsContainer.style.display = 'block';
                                actionsContainer.style.display = 'block';
                                submitButton.disabled = false;
                                rsvpForm.reset();
                            }, 500);
                        });
                    }
                })
                .catch(error => {
                    console.error('Errore dettagliato:', error);

                    // RIMUOVI L'OVERLAY ANCHE IN CASO DI ERRORE COSI L'UTENTE RI-VEDE I CAMPI DI TESTO
                    const overlay = document.getElementById('rsvpLoaderOverlay');
                    if (overlay) overlay.remove();

                    // Sblocca il pulsante così l'utente può tentare di nuovo l'invio
                    if (submitButton) {
                        submitButton.disabled = false;
                        submitButton.value = "Conferma la tua presenza";
                        submitButton.style.opacity = "1";
                        submitButton.style.cursor = "pointer";
                    }

                    if (feedback) {
                        feedback.innerHTML = '<span style="color: #ffaaaa;"><i class="fas fa-exclamation-triangle"></i> Ops! Qualcosa è andato storto nell\'invio. Riprova tra un momento.<br/><small style="opacity: 0.7;">Dettaglio: ' + error.message + '</small></span>';
                        setTimeout(() => { feedback.innerHTML = ''; }, 6000);
                    }
                });
        });
    } else {
        console.error('Form RSVP non trovato!');
    }

    // Stato iniziale
    main.style.display = 'none';
    articles.forEach(art => art.style.display = 'none');
    header.style.display = 'flex';
    footer.style.display = 'block';

    // Rimuovi preload
    setTimeout(() => {
        body.classList.remove('is-preload');
    }, 100);

    console.log('Inizializzazione completata');
});