package com.example.ui

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.data.LicenseStatus
import com.example.ui.OrchidDeepStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class SovereignLawsuitHandler {

    companion object {
        private const val TAG = "SovereignLawsuitHandler"

        /**
         * Initiates a civil lawsuit on behalf of Dr. Tim against a patient.
         */
        fun initiateCivilSuit(
            viewModel: SimulationViewModel,
            patientName: String,
            reason: String
        ) {
            viewModel.viewModelScope.launch {
                try {
                    // 1. Configure the states
                    OrchidDeepStateManager.setLawsuitType(isPlaintiff = true, patientName = patientName, reason = reason)
                    OrchidDeepStateManager.setCourtroomLevel(1)
                    
                    // Reset trial rounds and clear parameters
                    OrchidDeepStateManager.resetTrialRounds(3)
                    viewModel.clearJustificationLaws()
                    
                    // 2. Clear previous trial states
                    viewModel.updateLawsuitDetails(
                        name = patientName,
                        diagnosis = "Past Patient Case Records",
                        violations = emptyList() // Civil suits don't have policy audit violations initially
                    )
                    
                    // Configure charges/complaints based on selected reason
                    val complaintsList = when (reason) {
                        "Fee Evasion / Billing Default" -> listOf(
                            "CIVIL COMPLAINT: Patient $patientName willfully defaulted and avoided payment of contractually-stipulated outpatient consult fees.",
                            "FINANCIAL DAMAGE: Clinic incurred direct financial depletion of unpaid consultative professional hours & bedside stock.",
                            "RESTITUTION RELIEF: Demanding compensatory damages for fee recovery under Pretoria civil codes."
                        )
                        "Character Defamation & Slander" -> listOf(
                            "CIVIL COMPLAINT: Defendant $patientName published unfounded and false professional clinical allegations maliciously.",
                            "REPUTATION IMPAIRMENT: Clinical goodwill severely damaged, causing an immediate drop in local clinic patient inquiries.",
                            "PUNITIVE SPEC RELIEF: Claiming R3,000 for professional and business character damage."
                        )
                        else -> listOf(
                            "CIVIL COMPLAINT: Defendant $patientName attempted extortive malpractice claims to acquire high-schedule substances bad-faith.",
                            "BUSINESS INTERRUPTION: Forced diversion of medical executive clinical focus to prepare regulatory defense assets.",
                            "RESTRAINING DECLARATIVE: Seeking a formal court order and damages for malicious regulatory harassment."
                        )
                    }
                    viewModel.setLawsuitCharges(complaintsList)

                    // Provide forensic explanation patient log
                    val patientRecordsLog = """
                        === CIVIL SUIT LITIGATION DISCLOSURE FILE ===
                        Plaintiff: Dr. Tim (Consulting Clinic Practitioner)
                        Defendant Patient: $patientName
                        Litigation Action Basis: $reason
                        
                        This lawsuit seeks compensatory damages for active financial or reputational injury inflicted on Dr. Tim's consulting practice.
                    """.trimIndent()
                    viewModel.setCourtroomPatientLog(patientRecordsLog)

                    // Setup initial transcript logs
                    val initialLogs = listOf(
                        "🏛️ PRETORIA CIVIL DISTRICT MAGISTRATE COURT",
                        "CIVIL PETITION FILED: Dr. Tim vs. $patientName",
                        "CLAIMS SUBMITTED: The plaintiff has registered a civil action seeking formal restitution and damages for: $reason.\n\nThe defendant patient has entered a general appearance to defend. Prepare your pleadings and selection of supportive clinical exhibits to convince the Judge & Civil Jury panel."
                    )
                    viewModel.setLawsuitLog(initialLogs)

                    // Initialize the jury panel for Level 1 District Court
                    val fullJurorPool = listOf(
                        Juror("Evelyn Vance", "Foreperson - High School Principal", "Undecided", "Reviewing Dr. Tim's civil claims..."),
                        Juror("Kofi Mensah", "Aeronautical Engineer", "Undecided", "Awaiting clinical explanation."),
                        Juror("Aunt Sarah", "Retired Ward Nurse", "Favorable", "Evaluating patient responsibility."),
                        Juror("Dmitri Romanov", "Construction Contractor", "Skeptical", "Concerned about contract enforcement."),
                        Juror("Thabo Dube", "Financial Auditor", "Undecided", "Reviewing invoice/default margins."),
                        Juror("Priya Patel", "Highschool Biology Teacher", "Undecided", "Analyzing public statements.")
                    )
                    viewModel.courtroomViewModel.updateJurors(fullJurorPool.shuffled().take(4))

                    // Open the courtroom dialog
                    viewModel.setLawsuitActive(true)
                    viewModel.setLawsuitCurrentStage("charges")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error initiating civil lawsuit: ${e.localizedMessage}", e)
                }
            }
        }

        /**
         * Escalates current trial to a higher tier court in a cascading hearing appeal.
         */
        fun appealCurrentVerdict(viewModel: SimulationViewModel) {
            val currentLevel = OrchidDeepStateManager.courtroomLevel.value
            if (currentLevel >= 3) {
                viewModel.logAndEmitError("You have reached the supreme apex level! No higher judicial court of appeal exists under the constitution.")
                return
            }

            val isPlaintiff = OrchidDeepStateManager.lawsuitIsPlaintiff.value
            val patientName = viewModel.lawsuitPatientName.value
            val costOfAppeal = 500.0

            if (viewModel.clinicBalance.value < costOfAppeal) {
                viewModel.logAndEmitError("Insufficient funds! Lodge appeal requires R$costOfAppeal in judicial administrative costs.")
                return
            }

            viewModel.viewModelScope.launch {
                try {
                    // Pay appeal fee
                    viewModel.updateClinicBalance(viewModel.clinicBalance.value - costOfAppeal)
                    viewModel.registerDailyExpense(costOfAppeal)

                    val nextLevel = currentLevel + 1
                    OrchidDeepStateManager.setCourtroomLevel(nextLevel)

                    // Reset state for new round of pleadings
                    OrchidDeepStateManager.resetTrialRounds(3)
                    viewModel.clearJustificationLaws()
                    
                    // Reset tension / aggression to modern initial standards for the new high-tier court
                    viewModel.resetLawsuitTensionAndAggression(
                        tension = if (isPlaintiff) 45 else 55,
                        aggression = if (isPlaintiff) 40 else 60
                    )

                    val nextCourtName = if (isPlaintiff) {
                        if (nextLevel == 2) "🏛️ SOVEREIGN HIGH CIVIL COURT" else "⚖️ APEX COURT OF CIVIL APPEALS"
                    } else {
                        if (nextLevel == 2) "🏛️ NATIONAL HEALTH REGULATORY TRIBUNAL" else "⚖️ SUPREME MEDICAL APPEALS COUNCIL"
                    }

                    val charges = viewModel.lawsuitCharges.value
                    val initialLogs = listOf(
                        "⚖️ CASE APPEALED TO HIGHER JUDICIAL TIER",
                        "COURT OF RECORD: $nextCourtName",
                        "The previous verdict has been formally stayed. A larger civil jury panel of ${if (nextLevel == 2) 5 else 6} has been empaneled to review the entire clinical record de novo.",
                        "Appeal Costs Settled: R$costOfAppeal has been deducted from clinic coffers. Present your new pleadings."
                    )
                    viewModel.setLawsuitLog(initialLogs)
                    viewModel.setLawsuitCurrentStage("charges")

                    // Re-initialize jurors with larger panel for higher-tier court
                    val fullJurorPool = listOf(
                        Juror("Evelyn Vance", "Foreperson - High School Principal", "Undecided", "Assessing appeal evidence..."),
                        Juror("Kofi Mensah", "Aeronautical Engineer", "Undecided", "Analyzing procedural consistency."),
                        Juror("Aunt Sarah", "Retired Ward Nurse", "Favorable", "Checking standard guidelines."),
                        Juror("Dmitri Romanov", "Construction Contractor", "Skeptical", "Looking for verified facts."),
                        Juror("Thabo Dube", "Financial Auditor", "Undecided", "Verifying economic damage levels."),
                        Juror("Priya Patel", "Highschool Biology Teacher", "Undecided", "Reviewing clinical ethics and outcomes.")
                    )
                    viewModel.courtroomViewModel.updateJurors(fullJurorPool.shuffled().take(if (nextLevel == 2) 5 else 6))
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing courtroom appeal: ${e.localizedMessage}", e)
                }
            }
        }

        /**
         * Resolves verdict prompt specifically modeled for doctor-led civil active lawsuits.
         */
        fun buildCivilVerdictPrompt(
            viewModel: SimulationViewModel,
            countryName: String,
            patientName: String,
            reason: String,
            patientRecordsLog: String,
            currentHistoryLog: String,
            jurySentiment: Int,
            tension: Int,
            aggression: Int,
            selectedJustify: List<String>,
            activeSeledEvid: List<String>,
            hiredLawyerDisplayName: String?
        ): String {
            val courtLevel = OrchidDeepStateManager.courtroomLevel.value
            val courtName = when (courtLevel) {
                1 -> "CIVIL DISTRICT MAGISTRATE COURT"
                2 -> "SOVEREIGN HIGH CIVIL COURT"
                else -> "APEX COURT OF CIVIL APPEALS"
            }

            return """
                You are the Presiding Judicial Gavel and Chief Arbitrist of the $courtName of $countryName.
                You are rendering final judgment in the civil damages litigation filed by Plaintiff Dr. Tim against Defendant Patient "$patientName".
                
                CAUSE OF ACTION BY PLAINTIFF:
                - Basis for Lawsuit claim: $reason
                
                SUPPORTING CLINICAL RECORDS EXPOSURES:
                $patientRecordsLog
                
                LITIGATION TRIAL TRANSCRIPT HISTORY:
                $currentHistoryLog
                
                EVIDENTIARY EXHIBITS & PLEADINGS AND DEBATES SUBMITTED:
                - Selected evidence/exhibits submitted by doctor: ${if (activeSeledEvid.isNotEmpty()) activeSeledEvid.joinToString(", ") else "None"}
                - Doctor's Counsel: ${hiredLawyerDisplayName ?: "Self-Represented GP"}
                - Defendant Patient's Legal Objections & Counterclaims (simulated within reasoning): High
                - Court Tension Level: $tension%
                - Defense Opposition Aggression Level: $aggression%
                - Jury Evaluation Sentiment Panel: $jurySentiment% (Above 50% favors Plaintiff Dr. Tim, below 50% favors Defendant Patient)
                
                JUDICIAL RATIONALE PROTOCOLS:
                1. Evaluate the merits of Dr. Tim's restitution claim. If the defendant defaulted on payments or defamed the clinic, Dr. Tim should win damages.
                2. If Jury sentiment >= 55% and tension level <= 55%, rule "Damages Awarded" favoring Dr. Tim.
                3. If the claimant failed to substantiate their claims, or if jury sentiment is low, rule "Dismissed".
                
                VERDICT OUTCOME SCHEMA AND CONSTRAINTS:
                - Allowed verdictType: "Damages Awarded" or "Dismissed".
                - If "Damages Awarded", define a positive cash damages settlement to be paid by patient to Dr. Tim (range: R1000.00 to R4500.00). Keep "suspensionWeeks" as 0.
                - If "Dismissed", Dr. Tim receives nothing. "fineAmount" must be 0.0 and "suspensionWeeks" as 0.
                
                Return VALID strictly raw JSON block (no enclosing markdown or code backticks, just raw json) matching the following:
                {
                   "verdictType": "Damages Awarded",
                   "fineAmount": 2800.0,
                   "suspensionWeeks": 0,
                   "finalVerdictText": "Outlining formal judicial final award. Cite the evidence presented, describe the reasoning of the court, reject the defendant's counter-pleas, and decree the compensation/damages award ordered."
                }
            """.trimIndent()
        }

        /**
         * Initiates a criminal trial on behalf of the National Prosecution Counsel against Dr. Tim.
         */
        fun initiateCriminalTrial(
            viewModel: SimulationViewModel,
            patientName: String,
            reason: String,
            severityLevel: Int = 1
        ) {
            viewModel.viewModelScope.launch {
                try {
                    OrchidDeepStateManager.setLawsuitType(isPlaintiff = false, patientName = patientName, reason = reason)
                    OrchidDeepStateManager.setLawsuitCriminal(true)
                    OrchidDeepStateManager.setCourtroomLevel(severityLevel)
                    OrchidDeepStateManager.resetTrialRounds(3)
                    viewModel.clearJustificationLaws()
                    
                    viewModel.updateLawsuitDetails(
                        name = patientName,
                        diagnosis = "Severe Statutory Crimes Evaluation",
                        violations = emptyList()
                    )
                    
                    val indictmentList = when (reason) {
                        "Unauthorized Narcotic Compounding & Trafficking" -> listOf(
                            "CRIMINAL CHARGE: Unauthorized creation, modification, or mass stockpiling of high-schedule chemical agents.",
                            "PUBLIC HAZARD: Placing dangerous synthesized substances in municipal reach without national clinical certificates.",
                            "FELONY COMPLIANCE DEVIATION: Bypassing standard therapeutic approval acts."
                        )
                        "Regulatory Extortion Subversion" -> listOf(
                            "CRIMINAL CHARGE: Intentional subversion of state compliance agents, bribe infiltration, or regulatory interference.",
                            "FELONY CONSPIRACY: Active manipulation of insurance pre-authorization networks."
                        )
                        else -> listOf(
                            "CRIMINAL CHARGE: Sub-standard therapeutic negligence causing severe vegetative, metabolic, or respiratory impairment.",
                            "FELONY MEDICAL MALPRACTICE: Operating clinical bedside tests using unauthorized, non-certified tools."
                        )
                    }
                    viewModel.setLawsuitCharges(indictmentList)

                    val recordLog = """
                        === STATE CRIMINAL PROSECUTION BRIEF ===
                        Accused: Dr. Tim (General Practitioner)
                        Sovereign Grievance Category: $reason
                        Investigating Agency: Republic Medical Apprehension Division
                        
                        This prosecution seeks criminal professional license sanctions, state compliance fines, or formal imprisonment/clinical closure options under criminal medical protocol.
                    """.trimIndent()
                    viewModel.setCourtroomPatientLog(recordLog)

                    val initialLogs = listOf(
                        "🏛️ SUPREME REPUBLIC CRIMINAL MAGISTRATE TRIBUNAL",
                        "CONSTITUTIONAL WARRANT ISSUED FOR DR. TIM",
                        "INDICTMENT DETAILS: The State Prosecutor has registered severe criminal medical charges: $reason.\n\nA dynamic citizen jury has been empaneled to weigh evidence of bad-faith practice or negligent therapy."
                    )
                    viewModel.setLawsuitLog(initialLogs)

                    val fullJurorPool = listOf(
                        Juror("Evelyn Vance", "Foreperson - High School Principal", "Hostile", "Demanding standard judicial compliance."),
                        Juror("Kofi Mensah", "Aeronautical Engineer", "Skeptical", "Looking for verifiable forensic metrics."),
                        Juror("Aunt Sarah", "Retired Ward Nurse", "Skeptical", "Saddened by criminal professional allegations."),
                        Juror("Dmitri Romanov", "Construction Contractor", "Undecided", "Pragmatic assessment of guilt."),
                        Juror("Thabo Dube", "Financial Auditor", "Skeptical", "Scrutinizing possible billing or narcotic diversion."),
                        Juror("Priya Patel", "Highschool Biology Teacher", "Undecided", "Investigating chemical compounding formulas.")
                    )
                    viewModel.courtroomViewModel.updateJurors(fullJurorPool.shuffled().take(4))

                    // Open the courtroom dialog
                    viewModel.setLawsuitActive(true)
                    viewModel.setLawsuitCurrentStage("charges")
                    OrchidDeepStateManager.logAgenticOperation("Criminal lawsuit indictment initiated against practitioner for: $reason")
                } catch (e: Exception) {
                    Log.e(TAG, "Error initiating criminal trial: ${e.localizedMessage}", e)
                }
            }
        }

        /**
         * Resolves verdict prompt specifically modeled for state criminal prosecutorial trials.
         */
        fun buildCriminalVerdictPrompt(
            viewModel: SimulationViewModel,
            countryName: String,
            patientName: String,
            reason: String,
            patientRecordsLog: String,
            currentHistoryLog: String,
            jurySentiment: Int,
            tension: Int,
            aggression: Int,
            selectedJustify: List<String>,
            activeSeledEvid: List<String>,
            hiredLawyerDisplayName: String?
        ): String {
            val courtLevel = OrchidDeepStateManager.courtroomLevel.value
            val courtName = when (courtLevel) {
                1 -> "NATIONAL CRIMINAL INQUEST MAGISTRATE"
                2 -> "HIGH CRIMES TRIAL COURT"
                else -> "APEX COURT OF CRIMINAL RECOURSE"
            }

            return """
                You are the Presiding Supreme Justice of the criminal medical division of the ${courtName} of ${countryName}, presiding over the Criminal indictment of Defendant Dr. Tim.
                
                CRIMINAL GRIEVANCE OFFENSE IN ACCOUNT:
                - Offense category: $reason
                
                CIVIL BRIEF EXPOSURES & EVIDENCE FILE:
                $patientRecordsLog
                
                TRIAL TRANSCRIPTS HISTORY LOG:
                $currentHistoryLog
                
                EVIDENTIARY EXHIBITS & PLEADINGS AND DEBATES SUBMITTED:
                - Selected evidence/exhibits submitted by doctor: ${if (activeSeledEvid.isNotEmpty()) activeSeledEvid.joinToString(", ") else "None"}
                - Doctor's Counsel: ${hiredLawyerDisplayName ?: "Self-Represented Accused Practitioner"}
                - Defendant's Cited Justifications: ${if (selectedJustify.isNotEmpty()) selectedJustify.joinToString(", ") else "None"}
                - Court Tension Level: $tension%
                - Prosecution Aggression Level: $aggression%
                - Citizen Jury evaluation sentiment: $jurySentiment% (Above 55% favors acquittal, below 45% favors severe criminal verdict)
                
                JUDICIAL RATIONALE PROTOCOLS:
                1. Evaluate whether the defense counsel and exhibits successfully disproved statutory negligence or criminal bad-faith compounding. 
                2. If Jury sentiment >= 60% and tension <= 50%, rule "Acquitted".
                3. If there is a high-schedule substance violation or regulatory subversion found in the performance log, or if jury sentiment is low, rule "Guilty".
                
                VERDICT OUTCOME SCHEMA AND CONSTRAINTS:
                - Allowed verdictType: "Acquitted", "Guilty - Penal Fine", or "Guilty - License Suspended".
                - If "Guilty - Penal Fine", define a positive cash fine to be paid to state coffers (range: R2000.00 to R8000.00). Keep "suspensionWeeks" as 0.
                - If "Guilty - License Suspended", define the suspension weeks (range: 1 to 4 weeks).
                
                Return VALID strictly raw JSON block (no enclosing markdown or code backticks, just raw json) matching the following:
                {
                   "verdictType": "Acquitted",
                   "fineAmount": 0.0,
                   "suspensionWeeks": 0,
                   "finalVerdictText": "Judicial Sentencing Decree. Formally outline the criminal case findings, cite code violations, evaluate client pleadings, announce the guilt or acquittal, and declare the state penalty."
                }
            """.trimIndent()
        }
    }
}
