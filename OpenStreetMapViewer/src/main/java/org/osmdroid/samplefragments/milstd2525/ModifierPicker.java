package org.osmdroid.samplefragments.milstd2525;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import org.osmdroid.R;

import armyc2.c2sd.renderer.utilities.ModifiersTG;
import armyc2.c2sd.renderer.utilities.ModifiersUnits;
import armyc2.c2sd.renderer.utilities.RendererSettings;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;

/**
 * created on 1/30/2018.
 *
 * @author Alex O'Ree
 */

public class ModifierPicker implements View.OnClickListener, TextWatcher {


    public ModifierPicker() {

    }

    AlertDialog picker = null;
    Button milstd_search_cancel = null;

    EditText milstd_search = null;
    RadioButton milstd_search_affil_f = null;
    RadioButton milstd_search_affil_h = null;
    RadioButton milstd_search_affil_n = null;
    RadioButton milstd_search_affil_u = null;

    String charAffiliation = "F";

    public void destroy() {
        if (picker != null) {
            picker.dismiss();
        }
        picker = null;

        milstd_search_cancel = null;

        milstd_search = null;
    }

    SimpleSymbol symbol;
    EditText AM_DISTANCE_edit = null;
    EditText AN_AZIMUTH_edit = null;
    EditText ANGLE_edit = null;
    EditText C_QUANTITY_edit = null;
    EditText H_ADDITIONAL_INFO_1_edit = null;
    EditText H1_ADDITIONAL_INFO_2_edit = null;
    EditText H2_ADDITIONAL_INFO_3_edit = null;
    EditText LENGTH_edit = null;
    EditText RADIUS_edit = null;
    EditText S_OFFSET_INDICATOR_edit = null;
    EditText W1_DTG_2_edit = null;
    EditText W_DTG_1_edit = null;
    EditText D_TASK_FORCE_INDICATOR_edit = null;
    EditText E_FRAME_SHAPE_MODIFIER_edit = null;
    EditText F_REINFORCED_REDUCED_edit = null;
    EditText G_STAFF_COMMENTS_edit = null;
    EditText J_EVALUATION_RATING_edit = null;
    EditText K_COMBAT_EFFECTIVENESS_edit = null;
    EditText L_SIGNATURE_EQUIP_edit = null;
    EditText M_HIGHER_FORMATION_edit = null;
    EditText N_HOSTILE_edit = null;
    EditText P_IFF_SIF_edit = null;
    EditText Q_DIRECTION_OF_MOVEMENT_edit = null;
    EditText R2_SIGNIT_MOBILITY_INDICATOR_edit = null;
    EditText T1_UNIQUE_DESIGNATION_2_edit = null;
    EditText T_UNIQUE_DESIGNATION_1_edit = null;
    EditText V_EQUIP_TYPE_edit = null;
    EditText X_ALTITUDE_DEPTH_edit = null;
    EditText Z_SPEED_edit = null;
    EditText AA_SPECIAL_C2_HQ_edit = null;
    EditText AB_FEINT_DUMMY_INDICATOR_edit = null;
    EditText AC_INSTALLATION_edit = null;
    EditText AD_PLATFORM_TYPE_edit = null;
    EditText AE_EQUIPMENT_TEARDOWN_TIME_edit = null;
    EditText AF_COMMON_IDENTIFIER_edit = null;
    EditText AG_AUX_EQUIP_INDICATOR_edit = null;
    EditText AH_AREA_OF_UNCERTAINTY_edit = null;
    EditText AI_DEAD_RECKONING_TRAILER_edit = null;
    EditText AJ_SPEED_LEADER_edit = null;
    EditText AK_PAIRING_LINE_edit = null;
    EditText AL_OPERATIONAL_CONDITION_edit = null;
    EditText AO_ENGAGEMENT_BAR_edit = null;
    EditText SCC_SONAR_CLASSIFICATION_CONFIDENCE_edit = null;
    EditText CN_CPOF_NAME_LABEL_edit = null;
    EditText COUNTRY_CODE_edit = null;
    Button milstd_modifier_apply = null;
    Spinner echelon1, echelon2 = null;

    public void show(Activity activity, SimpleSymbol symbol) {
        if (picker != null) {
            picker.show();
            return;
        }
        this.symbol = symbol;
        //prompt for input params
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View view = View.inflate(activity, R.layout.milstd2525modifiers, null);

        milstd_modifier_apply = view.findViewById(R.id.milstd_modifier_apply);
        milstd_modifier_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyModifiers();
            }
        });
        milstd_search_affil_f = view.findViewById(R.id.milstd_search_affil_f);
        milstd_search_affil_h = view.findViewById(R.id.milstd_search_affil_h);
        milstd_search_affil_n = view.findViewById(R.id.milstd_search_affil_n);
        milstd_search_affil_u = view.findViewById(R.id.milstd_search_affil_u);

        COUNTRY_CODE_edit = view.findViewById(R.id.COUNTRY_edit);
        AM_DISTANCE_edit = view.findViewById(R.id.AM_DISTANCE_edit);
        AN_AZIMUTH_edit = view.findViewById(R.id.AN_AZIMUTH_edit);
        ANGLE_edit = view.findViewById(R.id.ANGLE_edit);
        C_QUANTITY_edit = view.findViewById(R.id.C_QUANTITY_edit);
        H_ADDITIONAL_INFO_1_edit = view.findViewById(R.id.H_ADDITIONAL_INFO_1_edit);
        H1_ADDITIONAL_INFO_2_edit = view.findViewById(R.id.H1_ADDITIONAL_INFO_2_edit);
        H2_ADDITIONAL_INFO_3_edit = view.findViewById(R.id.H2_ADDITIONAL_INFO_3_edit);
        LENGTH_edit = view.findViewById(R.id.LENGTH_edit);
        N_HOSTILE_edit = view.findViewById(R.id.N_HOSTILE_edit);
        RADIUS_edit = view.findViewById(R.id.RADIUS_edit);
        Q_DIRECTION_OF_MOVEMENT_edit = view.findViewById(R.id.Q_DIRECTION_OF_MOVEMENT_edit);
        S_OFFSET_INDICATOR_edit = view.findViewById(R.id.S_OFFSET_INDICATOR_edit);
        V_EQUIP_TYPE_edit = view.findViewById(R.id.V_EQUIP_TYPE_edit);
        W1_DTG_2_edit = view.findViewById(R.id.W1_DTG_2_edit);
        W_DTG_1_edit = view.findViewById(R.id.W_DTG_1_edit);
        T1_UNIQUE_DESIGNATION_2_edit = view.findViewById(R.id.T1_UNIQUE_DESIGNATION_2_edit);
        T_UNIQUE_DESIGNATION_1_edit = view.findViewById(R.id.T_UNIQUE_DESIGNATION_1_edit);


        D_TASK_FORCE_INDICATOR_edit = view.findViewById(R.id.D_TASK_FORCE_INDICATOR_edit);
        E_FRAME_SHAPE_MODIFIER_edit = view.findViewById(R.id.E_FRAME_SHAPE_MODIFIER_edit);
        F_REINFORCED_REDUCED_edit = view.findViewById(R.id.F_REINFORCED_REDUCED_edit);
        G_STAFF_COMMENTS_edit = view.findViewById(R.id.G_STAFF_COMMENTS_edit);
        J_EVALUATION_RATING_edit = view.findViewById(R.id.J_EVALUATION_RATING_edit);
        K_COMBAT_EFFECTIVENESS_edit = view.findViewById(R.id.K_COMBAT_EFFECTIVENESS_edit);
        L_SIGNATURE_EQUIP_edit = view.findViewById(R.id.L_SIGNATURE_EQUIP_edit);


        M_HIGHER_FORMATION_edit = view.findViewById(R.id.M_HIGHER_FORMATION_edit);
        N_HOSTILE_edit = view.findViewById(R.id.N_HOSTILE_edit);
        P_IFF_SIF_edit = view.findViewById(R.id.P_IFF_SIF_edit);
        Q_DIRECTION_OF_MOVEMENT_edit = view.findViewById(R.id.Q_DIRECTION_OF_MOVEMENT_edit);
        R2_SIGNIT_MOBILITY_INDICATOR_edit = view.findViewById(R.id.R2_SIGNIT_MOBILITY_INDICATOR_edit);
        T1_UNIQUE_DESIGNATION_2_edit = view.findViewById(R.id.T1_UNIQUE_DESIGNATION_2_edit);
        T_UNIQUE_DESIGNATION_1_edit = view.findViewById(R.id.T_UNIQUE_DESIGNATION_1_edit);
        V_EQUIP_TYPE_edit = view.findViewById(R.id.V_EQUIP_TYPE_edit);
        X_ALTITUDE_DEPTH_edit = view.findViewById(R.id.X_ALTITUDE_DEPTH_edit);
        Z_SPEED_edit = view.findViewById(R.id.Z_SPEED_edit);
        AA_SPECIAL_C2_HQ_edit = view.findViewById(R.id.AA_SPECIAL_C2_HQ_edit);
        AB_FEINT_DUMMY_INDICATOR_edit = view.findViewById(R.id.AB_FEINT_DUMMY_INDICATOR_edit);
        AC_INSTALLATION_edit = view.findViewById(R.id.AC_INSTALLATION_edit);
        AD_PLATFORM_TYPE_edit = view.findViewById(R.id.AD_PLATFORM_TYPE_edit);
        AE_EQUIPMENT_TEARDOWN_TIME_edit = view.findViewById(R.id.AE_EQUIPMENT_TEARDOWN_TIME_edit);
        AF_COMMON_IDENTIFIER_edit = view.findViewById(R.id.AF_COMMON_IDENTIFIER_edit);
        AG_AUX_EQUIP_INDICATOR_edit = view.findViewById(R.id.AG_AUX_EQUIP_INDICATOR_edit);
        AH_AREA_OF_UNCERTAINTY_edit = view.findViewById(R.id.AH_AREA_OF_UNCERTAINTY_edit);
        AI_DEAD_RECKONING_TRAILER_edit = view.findViewById(R.id.AI_DEAD_RECKONING_TRAILER_edit);
        AJ_SPEED_LEADER_edit = view.findViewById(R.id.AJ_SPEED_LEADER_edit);
        AK_PAIRING_LINE_edit = view.findViewById(R.id.AK_PAIRING_LINE_edit);
        AL_OPERATIONAL_CONDITION_edit = view.findViewById(R.id.AL_OPERATIONAL_CONDITION_edit);
        AO_ENGAGEMENT_BAR_edit = view.findViewById(R.id.AO_ENGAGEMENT_BAR_edit);


        SCC_SONAR_CLASSIFICATION_CONFIDENCE_edit = view.findViewById(R.id.SCC_SONAR_CLASSIFICATION_CONFIDENCE_edit);


        CN_CPOF_NAME_LABEL_edit = view.findViewById(R.id.CN_CPOF_NAME_LABEL_edit);
        //TODO set spinner adapters for echelons

        echelon1 = view.findViewById(R.id.echelon1);
        echelon1.setAdapter(new ArrayAdapter<SimpleSymbol.Echelon1>(activity, android.R.layout.simple_spinner_item, SimpleSymbol.Echelon1.values()));

        echelon2 = view.findViewById(R.id.echelon2);
        echelon2.setAdapter(new ArrayAdapter<SimpleSymbol.Echelon2>(activity, android.R.layout.simple_spinner_item, SimpleSymbol.Echelon2.values()));
        String baseCode = symbol.getBasicSymbolId();

        applyVisibility(baseCode, view);


        builder.setView(view);
        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                picker.dismiss();
            }
        });
        picker = builder.create();
        picker.show();


    }

    static boolean isDefined(EditText e) {
        String content = e.getText().toString();
        if (content == null || content.length() == 0)
            return false;
        return true;

    }

    private void applyModifiers() {
        String baseCode = symbol.getBasicSymbolId();
        SparseArray<String> modifiers = symbol.getModifiers();
        modifiers.clear();
        if (baseCode.charAt(0) != 'W') {
            //apply country code and echelons 1 and 2
            String code = symbol.getSymbolCode();

            SimpleSymbol.Echelon1 e1 = (SimpleSymbol.Echelon1) echelon1.getSelectedItem();
            SimpleSymbol.Echelon2 e2 = (SimpleSymbol.Echelon2) echelon2.getSelectedItem();
            String countryCode = COUNTRY_CODE_edit.getText().toString();
            if (countryCode != null && countryCode.length() == 2) {
                countryCode = countryCode.toUpperCase();
            } else countryCode = "--";
            code = code.substring(0, 10) + e1.getValue() + e2.getValue() + countryCode + "-";//FIXME

            //index 10 = echelon 1
            //index 11 = echelon 2
            //index 12-13 country code
            //index 14 orderbat
        }

        if (baseCode.charAt(0) == 'G' || baseCode.charAt(0) == 'W') {
            if (isDefined(AM_DISTANCE_edit))
                modifiers.put(ModifiersTG.AM_DISTANCE, AM_DISTANCE_edit.getText().toString());
            if (isDefined(AM_DISTANCE_edit))
                modifiers.put(ModifiersTG.AN_AZIMUTH, AN_AZIMUTH_edit.getText().toString());
            if (isDefined(ANGLE_edit))
                modifiers.put(ModifiersTG.ANGLE, ANGLE_edit.getText().toString());
            if (isDefined(C_QUANTITY_edit))
                modifiers.put(ModifiersTG.C_QUANTITY, C_QUANTITY_edit.getText().toString());
            if (isDefined(H_ADDITIONAL_INFO_1_edit))
                modifiers.put(ModifiersTG.H_ADDITIONAL_INFO_1, H_ADDITIONAL_INFO_1_edit.getText().toString());
            if (isDefined(H1_ADDITIONAL_INFO_2_edit))
                modifiers.put(ModifiersTG.H1_ADDITIONAL_INFO_2, H1_ADDITIONAL_INFO_2_edit.getText().toString());
            if (isDefined(H2_ADDITIONAL_INFO_3_edit))
                modifiers.put(ModifiersTG.H2_ADDITIONAL_INFO_3, H2_ADDITIONAL_INFO_3_edit.getText().toString());
            if (isDefined(LENGTH_edit))
                modifiers.put(ModifiersTG.LENGTH, LENGTH_edit.getText().toString());
            if (isDefined(N_HOSTILE_edit))
                modifiers.put(ModifiersTG.N_HOSTILE, N_HOSTILE_edit.getText().toString());
            if (isDefined(Q_DIRECTION_OF_MOVEMENT_edit))
                modifiers.put(ModifiersTG.Q_DIRECTION_OF_MOVEMENT, Q_DIRECTION_OF_MOVEMENT_edit.getText().toString());
            if (isDefined(RADIUS_edit))
                modifiers.put(ModifiersTG.RADIUS, RADIUS_edit.getText().toString());
            if (isDefined(S_OFFSET_INDICATOR_edit))
                modifiers.put(ModifiersTG.S_OFFSET_INDICATOR, S_OFFSET_INDICATOR_edit.getText().toString());
            if (isDefined(V_EQUIP_TYPE_edit))
                modifiers.put(ModifiersTG.V_EQUIP_TYPE, V_EQUIP_TYPE_edit.getText().toString());
            if (isDefined(W1_DTG_2_edit))
                modifiers.put(ModifiersTG.W1_DTG_2, W1_DTG_2_edit.getText().toString());
            if (isDefined(W_DTG_1_edit))
                modifiers.put(ModifiersTG.W_DTG_1, W_DTG_1_edit.getText().toString());
            if (isDefined(T1_UNIQUE_DESIGNATION_2_edit))
                modifiers.put(ModifiersTG.T1_UNIQUE_DESIGNATION_2, T1_UNIQUE_DESIGNATION_2_edit.getText().toString());
            if (isDefined(T_UNIQUE_DESIGNATION_1_edit))
                modifiers.put(ModifiersTG.T_UNIQUE_DESIGNATION_1, T_UNIQUE_DESIGNATION_1_edit.getText().toString());
            if (isDefined(X_ALTITUDE_DEPTH_edit))
                modifiers.put(ModifiersTG.X_ALTITUDE_DEPTH, X_ALTITUDE_DEPTH_edit.getText().toString());

        } else {
            if (isDefined(C_QUANTITY_edit))
                modifiers.put(ModifiersUnits.C_QUANTITY, C_QUANTITY_edit.getText().toString());

            if (isDefined(D_TASK_FORCE_INDICATOR_edit))
                modifiers.put(ModifiersUnits.D_TASK_FORCE_INDICATOR, D_TASK_FORCE_INDICATOR_edit.getText().toString());

            if (isDefined(E_FRAME_SHAPE_MODIFIER_edit))
                modifiers.put(ModifiersUnits.E_FRAME_SHAPE_MODIFIER, E_FRAME_SHAPE_MODIFIER_edit.getText().toString());

            if (isDefined(F_REINFORCED_REDUCED_edit))
                modifiers.put(ModifiersUnits.F_REINFORCED_REDUCED, F_REINFORCED_REDUCED_edit.getText().toString());

            if (isDefined(G_STAFF_COMMENTS_edit))
                modifiers.put(ModifiersUnits.G_STAFF_COMMENTS, G_STAFF_COMMENTS_edit.getText().toString());

            if (isDefined(H_ADDITIONAL_INFO_1_edit))
                modifiers.put(ModifiersUnits.H_ADDITIONAL_INFO_1, H_ADDITIONAL_INFO_1_edit.getText().toString());

            if (isDefined(H1_ADDITIONAL_INFO_2_edit))
                modifiers.put(ModifiersUnits.H1_ADDITIONAL_INFO_2, H1_ADDITIONAL_INFO_2_edit.getText().toString());

            if (isDefined(H2_ADDITIONAL_INFO_3_edit))
                modifiers.put(ModifiersUnits.H2_ADDITIONAL_INFO_3, H2_ADDITIONAL_INFO_3_edit.getText().toString());


            if (isDefined(J_EVALUATION_RATING_edit))
                modifiers.put(ModifiersUnits.J_EVALUATION_RATING, J_EVALUATION_RATING_edit.getText().toString());


            if (isDefined(K_COMBAT_EFFECTIVENESS_edit))
                modifiers.put(ModifiersUnits.K_COMBAT_EFFECTIVENESS, K_COMBAT_EFFECTIVENESS_edit.getText().toString());


            if (isDefined(L_SIGNATURE_EQUIP_edit))
                modifiers.put(ModifiersUnits.L_SIGNATURE_EQUIP, L_SIGNATURE_EQUIP_edit.getText().toString());


            if (isDefined(M_HIGHER_FORMATION_edit))
                modifiers.put(ModifiersUnits.M_HIGHER_FORMATION, M_HIGHER_FORMATION_edit.getText().toString());


            if (isDefined(N_HOSTILE_edit))
                modifiers.put(ModifiersUnits.N_HOSTILE, N_HOSTILE_edit.getText().toString());


            if (isDefined(P_IFF_SIF_edit))
                modifiers.put(ModifiersUnits.P_IFF_SIF, P_IFF_SIF_edit.getText().toString());


            if (isDefined(Q_DIRECTION_OF_MOVEMENT_edit))
                modifiers.put(ModifiersUnits.Q_DIRECTION_OF_MOVEMENT, Q_DIRECTION_OF_MOVEMENT_edit.getText().toString());


            if (isDefined(R2_SIGNIT_MOBILITY_INDICATOR_edit))
                modifiers.put(ModifiersUnits.R2_SIGNIT_MOBILITY_INDICATOR, R2_SIGNIT_MOBILITY_INDICATOR_edit.getText().toString());


            if (isDefined(T1_UNIQUE_DESIGNATION_2_edit))
                modifiers.put(ModifiersUnits.T1_UNIQUE_DESIGNATION_2, T1_UNIQUE_DESIGNATION_2_edit.getText().toString());

            if (isDefined(T_UNIQUE_DESIGNATION_1_edit))
                modifiers.put(ModifiersUnits.T_UNIQUE_DESIGNATION_1, T_UNIQUE_DESIGNATION_1_edit.getText().toString());


            if (isDefined(V_EQUIP_TYPE_edit))
                modifiers.put(ModifiersUnits.V_EQUIP_TYPE, V_EQUIP_TYPE_edit.getText().toString());

            if (isDefined(V_EQUIP_TYPE_edit))
                modifiers.put(ModifiersUnits.V_EQUIP_TYPE, V_EQUIP_TYPE_edit.getText().toString());

            if (isDefined(X_ALTITUDE_DEPTH_edit))
                modifiers.put(ModifiersUnits.X_ALTITUDE_DEPTH, X_ALTITUDE_DEPTH_edit.getText().toString());


            if (isDefined(Z_SPEED_edit))
                modifiers.put(ModifiersUnits.Z_SPEED, Z_SPEED_edit.getText().toString());


            if (isDefined(AA_SPECIAL_C2_HQ_edit))
                modifiers.put(ModifiersUnits.AA_SPECIAL_C2_HQ, AA_SPECIAL_C2_HQ_edit.getText().toString());


            if (isDefined(AB_FEINT_DUMMY_INDICATOR_edit))
                modifiers.put(ModifiersUnits.AB_FEINT_DUMMY_INDICATOR, AB_FEINT_DUMMY_INDICATOR_edit.getText().toString());


            if (isDefined(AC_INSTALLATION_edit))
                modifiers.put(ModifiersUnits.AC_INSTALLATION, AC_INSTALLATION_edit.getText().toString());


            if (isDefined(AD_PLATFORM_TYPE_edit))
                modifiers.put(ModifiersUnits.AD_PLATFORM_TYPE, AD_PLATFORM_TYPE_edit.getText().toString());


            if (isDefined(AE_EQUIPMENT_TEARDOWN_TIME_edit))
                modifiers.put(ModifiersUnits.AE_EQUIPMENT_TEARDOWN_TIME, AE_EQUIPMENT_TEARDOWN_TIME_edit.getText().toString());


            if (isDefined(AF_COMMON_IDENTIFIER_edit))
                modifiers.put(ModifiersUnits.AF_COMMON_IDENTIFIER, AF_COMMON_IDENTIFIER_edit.getText().toString());


            if (isDefined(AG_AUX_EQUIP_INDICATOR_edit))
                modifiers.put(ModifiersUnits.AG_AUX_EQUIP_INDICATOR, AG_AUX_EQUIP_INDICATOR_edit.getText().toString());


            if (isDefined(AH_AREA_OF_UNCERTAINTY_edit))
                modifiers.put(ModifiersUnits.AH_AREA_OF_UNCERTAINTY, AH_AREA_OF_UNCERTAINTY_edit.getText().toString());


            if (isDefined(AI_DEAD_RECKONING_TRAILER_edit))
                modifiers.put(ModifiersUnits.AI_DEAD_RECKONING_TRAILER, AI_DEAD_RECKONING_TRAILER_edit.getText().toString());


            if (isDefined(AJ_SPEED_LEADER_edit))
                modifiers.put(ModifiersUnits.AJ_SPEED_LEADER, AJ_SPEED_LEADER_edit.getText().toString());


            if (isDefined(AK_PAIRING_LINE_edit))
                modifiers.put(ModifiersUnits.AK_PAIRING_LINE, AK_PAIRING_LINE_edit.getText().toString());


            if (isDefined(AL_OPERATIONAL_CONDITION_edit))
                modifiers.put(ModifiersUnits.AL_OPERATIONAL_CONDITION, AL_OPERATIONAL_CONDITION_edit.getText().toString());


            if (isDefined(AL_OPERATIONAL_CONDITION_edit))
                modifiers.put(ModifiersUnits.AL_OPERATIONAL_CONDITION, AL_OPERATIONAL_CONDITION_edit.getText().toString());


            if (isDefined(AL_OPERATIONAL_CONDITION_edit))
                modifiers.put(ModifiersUnits.AL_OPERATIONAL_CONDITION, AL_OPERATIONAL_CONDITION_edit.getText().toString());


            if (isDefined(AL_OPERATIONAL_CONDITION_edit))
                modifiers.put(ModifiersUnits.AL_OPERATIONAL_CONDITION, AL_OPERATIONAL_CONDITION_edit.getText().toString());
            if (isDefined(AO_ENGAGEMENT_BAR_edit))
                modifiers.put(ModifiersUnits.AO_ENGAGEMENT_BAR, AO_ENGAGEMENT_BAR_edit.getText().toString());

            if (isDefined(SCC_SONAR_CLASSIFICATION_CONFIDENCE_edit))
                modifiers.put(ModifiersUnits.SCC_SONAR_CLASSIFICATION_CONFIDENCE, SCC_SONAR_CLASSIFICATION_CONFIDENCE_edit.getText().toString());

            if (isDefined(CN_CPOF_NAME_LABEL_edit))
                modifiers.put(ModifiersUnits.CN_CPOF_NAME_LABEL, CN_CPOF_NAME_LABEL_edit.getText().toString());
        }

        //apply modifier
        picker.dismiss();
    }

    private void applyVisibility(String baseCode, View view) {
        if (baseCode.charAt(0) != 'W') {
            view.findViewById(R.id.COUNTRY_CODE).setVisibility(View.VISIBLE);
            view.findViewById(R.id.milstdspinner1).setVisibility(View.VISIBLE);
            view.findViewById(R.id.milstdspinner2).setVisibility(View.VISIBLE);
        }
        if (baseCode.charAt(0) == 'G' || baseCode.charAt(0) == 'W') {
            //SymbolDef symbolDef = SymbolDefTable.getInstance().getSymbolDef(baseCode, RendererSettings.getInstance().getSymbologyStandard());
            if (SymbolUtilities.canSymbolHaveModifier(baseCode, ModifiersTG.AM_DISTANCE)) {
                view.findViewById(R.id.AM_DISTANCE).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canSymbolHaveModifier(baseCode, ModifiersTG.AN_AZIMUTH, RendererSettings.getInstance().getSymbologyStandard())) {
                view.findViewById(R.id.AN_AZIMUTH).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canSymbolHaveModifier(baseCode, ModifiersTG.ANGLE, RendererSettings.getInstance().getSymbologyStandard())) {
                view.findViewById(R.id.ANGLE).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canSymbolHaveModifier(baseCode, ModifiersTG.C_QUANTITY, RendererSettings.getInstance().getSymbologyStandard())) {
                view.findViewById(R.id.C_QUANTITY).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canSymbolHaveModifier(baseCode, ModifiersTG.H_ADDITIONAL_INFO_1, RendererSettings.getInstance().getSymbologyStandard())) {
                view.findViewById(R.id.H_ADDITIONAL_INFO_1).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canSymbolHaveModifier(baseCode, ModifiersTG.H1_ADDITIONAL_INFO_2, RendererSettings.getInstance().getSymbologyStandard())) {
                view.findViewById(R.id.H1_ADDITIONAL_INFO_2).setVisibility(View.VISIBLE);
            }

            if (SymbolUtilities.canSymbolHaveModifier(baseCode, ModifiersTG.H2_ADDITIONAL_INFO_3, RendererSettings.getInstance().getSymbologyStandard())) {
                view.findViewById(R.id.H2_ADDITIONAL_INFO_3).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canSymbolHaveModifier(baseCode, ModifiersTG.LENGTH, RendererSettings.getInstance().getSymbologyStandard())) {
                view.findViewById(R.id.LENGTH).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canSymbolHaveModifier(baseCode, ModifiersTG.N_HOSTILE, RendererSettings.getInstance().getSymbologyStandard())) {
                view.findViewById(R.id.N_HOSTILE).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canSymbolHaveModifier(baseCode, ModifiersTG.Q_DIRECTION_OF_MOVEMENT, RendererSettings.getInstance().getSymbologyStandard())) {
                view.findViewById(R.id.Q_DIRECTION_OF_MOVEMENT).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canSymbolHaveModifier(baseCode, ModifiersTG.RADIUS, RendererSettings.getInstance().getSymbologyStandard())) {
                view.findViewById(R.id.RADIUS).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canSymbolHaveModifier(baseCode, ModifiersTG.S_OFFSET_INDICATOR, RendererSettings.getInstance().getSymbologyStandard())) {
                view.findViewById(R.id.S_OFFSET_INDICATOR).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canSymbolHaveModifier(baseCode, ModifiersTG.V_EQUIP_TYPE, RendererSettings.getInstance().getSymbologyStandard())) {
                view.findViewById(R.id.V_EQUIP_TYPE).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canSymbolHaveModifier(baseCode, ModifiersTG.W1_DTG_2, RendererSettings.getInstance().getSymbologyStandard())) {
                view.findViewById(R.id.W1_DTG_2).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canSymbolHaveModifier(baseCode, ModifiersTG.W_DTG_1, RendererSettings.getInstance().getSymbologyStandard())) {
                view.findViewById(R.id.W_DTG_1).setVisibility(View.VISIBLE);
            }


            if (SymbolUtilities.canSymbolHaveModifier(baseCode, ModifiersTG.T1_UNIQUE_DESIGNATION_2, RendererSettings.getInstance().getSymbologyStandard())) {
                view.findViewById(R.id.T1_UNIQUE_DESIGNATION_2).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canSymbolHaveModifier(baseCode, ModifiersTG.T_UNIQUE_DESIGNATION_1, RendererSettings.getInstance().getSymbologyStandard())) {
                view.findViewById(R.id.T_UNIQUE_DESIGNATION_1).setVisibility(View.VISIBLE);
            }

            if (SymbolUtilities.canSymbolHaveModifier(baseCode, ModifiersTG.X_ALTITUDE_DEPTH, RendererSettings.getInstance().getSymbologyStandard())) {
                view.findViewById(R.id.X_ALTITUDE_DEPTH).setVisibility(View.VISIBLE);
            }
        } else {
            //UnitDef def = UnitDefTable.getInstance().getUnitDef(baseCode, RendererSettings.getInstance().getSymbologyStandard());

            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.C_QUANTITY)) {
                view.findViewById(R.id.C_QUANTITY).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.D_TASK_FORCE_INDICATOR)) {
                view.findViewById(R.id.D_TASK_FORCE_INDICATOR).setVisibility(View.VISIBLE);
            }

            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.E_FRAME_SHAPE_MODIFIER)) {
                view.findViewById(R.id.E_FRAME_SHAPE_MODIFIER).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.F_REINFORCED_REDUCED)) {
                view.findViewById(R.id.F_REINFORCED_REDUCED).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.G_STAFF_COMMENTS)) {
                view.findViewById(R.id.G_STAFF_COMMENTS).setVisibility(View.VISIBLE);
            }

            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.H_ADDITIONAL_INFO_1)) {
                view.findViewById(R.id.H_ADDITIONAL_INFO_1).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.H1_ADDITIONAL_INFO_2)) {
                view.findViewById(R.id.H1_ADDITIONAL_INFO_2).setVisibility(View.VISIBLE);
            }

            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.H2_ADDITIONAL_INFO_3)) {
                view.findViewById(R.id.H2_ADDITIONAL_INFO_3).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.J_EVALUATION_RATING)) {
                view.findViewById(R.id.J_EVALUATION_RATING).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.K_COMBAT_EFFECTIVENESS)) {
                view.findViewById(R.id.K_COMBAT_EFFECTIVENESS).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.L_SIGNATURE_EQUIP)) {
                view.findViewById(R.id.L_SIGNATURE_EQUIP).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.M_HIGHER_FORMATION)) {
                view.findViewById(R.id.M_HIGHER_FORMATION).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.N_HOSTILE)) {
                view.findViewById(R.id.N_HOSTILE).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.P_IFF_SIF)) {
                view.findViewById(R.id.P_IFF_SIF).setVisibility(View.VISIBLE);
            }

            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.Q_DIRECTION_OF_MOVEMENT)) {
                view.findViewById(R.id.Q_DIRECTION_OF_MOVEMENT).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.R2_SIGNIT_MOBILITY_INDICATOR)) {
                view.findViewById(R.id.R2_SIGNIT_MOBILITY_INDICATOR).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.T1_UNIQUE_DESIGNATION_2)) {
                view.findViewById(R.id.T1_UNIQUE_DESIGNATION_2).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.T_UNIQUE_DESIGNATION_1)) {
                view.findViewById(R.id.T_UNIQUE_DESIGNATION_1).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.V_EQUIP_TYPE)) {
                view.findViewById(R.id.V_EQUIP_TYPE).setVisibility(View.VISIBLE);
            }

            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.X_ALTITUDE_DEPTH)) {
                view.findViewById(R.id.X_ALTITUDE_DEPTH).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.Z_SPEED)) {
                view.findViewById(R.id.Z_SPEED).setVisibility(View.VISIBLE);
            }
            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.AA_SPECIAL_C2_HQ)) {
                view.findViewById(R.id.AA_SPECIAL_C2_HQ).setVisibility(View.VISIBLE);
            }


            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.AB_FEINT_DUMMY_INDICATOR)) {
                view.findViewById(R.id.AB_FEINT_DUMMY_INDICATOR).setVisibility(View.VISIBLE);
            }

            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.AC_INSTALLATION)) {
                view.findViewById(R.id.AC_INSTALLATION).setVisibility(View.VISIBLE);
            }

            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.AD_PLATFORM_TYPE)) {
                view.findViewById(R.id.AD_PLATFORM_TYPE).setVisibility(View.VISIBLE);
            }

            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.AE_EQUIPMENT_TEARDOWN_TIME)) {
                view.findViewById(R.id.AE_EQUIPMENT_TEARDOWN_TIME).setVisibility(View.VISIBLE);
            }

            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.AF_COMMON_IDENTIFIER)) {
                view.findViewById(R.id.AF_COMMON_IDENTIFIER).setVisibility(View.VISIBLE);
            }

            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.AG_AUX_EQUIP_INDICATOR)) {
                view.findViewById(R.id.AG_AUX_EQUIP_INDICATOR).setVisibility(View.VISIBLE);
            }

            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.AH_AREA_OF_UNCERTAINTY)) {
                view.findViewById(R.id.AH_AREA_OF_UNCERTAINTY).setVisibility(View.VISIBLE);
            }

            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.AI_DEAD_RECKONING_TRAILER)) {
                view.findViewById(R.id.AI_DEAD_RECKONING_TRAILER).setVisibility(View.VISIBLE);
            }

            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.AJ_SPEED_LEADER)) {
                view.findViewById(R.id.AJ_SPEED_LEADER).setVisibility(View.VISIBLE);
            }

            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.AK_PAIRING_LINE)) {
                view.findViewById(R.id.AK_PAIRING_LINE).setVisibility(View.VISIBLE);
            }

            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.AL_OPERATIONAL_CONDITION)) {
                view.findViewById(R.id.AL_OPERATIONAL_CONDITION).setVisibility(View.VISIBLE);
            }

            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.AO_ENGAGEMENT_BAR)) {
                view.findViewById(R.id.AO_ENGAGEMENT_BAR).setVisibility(View.VISIBLE);
            }

            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.SCC_SONAR_CLASSIFICATION_CONFIDENCE)) {
                view.findViewById(R.id.SCC_SONAR_CLASSIFICATION_CONFIDENCE).setVisibility(View.VISIBLE);
            }

            if (SymbolUtilities.canUnitHaveModifier(baseCode, ModifiersUnits.CN_CPOF_NAME_LABEL)) {
                view.findViewById(R.id.CN_CPOF_NAME_LABEL).setVisibility(View.VISIBLE);
            }

        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.milstd_search_cancel:
                picker.dismiss();
                break;

            case R.id.milstd_search_affil_f:
                charAffiliation = "F";
                break;
            case R.id.milstd_search_affil_h:
                charAffiliation = "H";
                break;
            case R.id.milstd_search_affil_n:
                charAffiliation = "N";

                break;
            case R.id.milstd_search_affil_u:
                charAffiliation = "U";

                break;
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
