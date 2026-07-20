package com.example.quan_ly_du_an.feature_project.ui.member;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.quan_ly_du_an.R;
import com.example.quan_ly_du_an.feature_project.ui.project.ProjectViewModel;
import com.example.quan_ly_du_an.utils.RoleEnum;
import com.google.android.material.textfield.TextInputEditText;

public class AddMemberDialog extends DialogFragment {
    private static final String ARG_PROJECT_ID = "arg_project_id";
    private ProjectViewModel viewModel;
    private long projectId;

    private TextInputEditText edtMemberEmail;
    private RadioGroup rgRoleSelection;
    private Button btnCancel;
    private Button btnAddMemberSubmit;

    public static AddMemberDialog newInstance(long projectId) {
        AddMemberDialog fragment = new AddMemberDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getLong(ARG_PROJECT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_member, container, false);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        edtMemberEmail = view.findViewById(R.id.edtMemberEmail);
        rgRoleSelection = view.findViewById(R.id.rgRoleSelection);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnAddMemberSubmit = view.findViewById(R.id.btnAddMemberSubmit);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ProjectViewModel.class);

        btnCancel.setOnClickListener(v -> dismiss());

        btnAddMemberSubmit.setOnClickListener(v -> {
            String email = edtMemberEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                edtMemberEmail.setError("Vui lòng nhập Email!");
                return;
            }

            String selectedRole = RoleEnum.MEMBER.getRoleName();
            if (rgRoleSelection.getCheckedRadioButtonId() == R.id.rbRoleLeader) {
                selectedRole = RoleEnum.LEADER.getRoleName();
            }

            viewModel.addMember(projectId, email, selectedRole, () -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Đã thêm thành viên vào dự án!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    });
                }
            });
        });
    }
}